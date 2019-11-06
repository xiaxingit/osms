package com.one.school.service;

import com.google.gson.Gson;
import com.one.school.dao.StudentMapper;
import com.one.school.dao.TestPaperMapper;
import com.one.school.entity.Student;
import com.one.school.entity.TestPaper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import javax.annotation.Resource;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by lee on 2019/7/21.
 */

@Slf4j
@Service
public class TestPaperService {

    @Resource
    private TestPaperMapper testPaperMapper;
    @Resource
    private StudentMapper studentMapper;

    private Gson gson = new Gson();

    private static String COOKIE_TOKEN_KEY = "__RequestVerificationToken_L1F00";

    //基础url
    private static String BASE_URL = "http://opencourse.shtvu.org.cn";
    //登录url
    private static String LOGIN_URL = BASE_URL + "/Qt/Home/Login";
    //课程列表url
    private static String QT_INFO_URL = BASE_URL + "/Qt/";
    //公共课测试题目url
    private static String PUBLIC_SUBJECT_URL = BASE_URL + "/Qt/Home/DoTest?isSpecial=False";
    //专业课测试题目url
    private static String SPECIAL_SUBJECT_URL = BASE_URL + "/Qt/Home/DoTest?isSpecial=True";

    private static HashMap<String, String> personMap = new HashMap<>();

    private static List<Integer> publicScoreList = Arrays.asList(48,50,52,54,56,58,60,62,64,66);
    private static List<Integer> specialityScoreList = Arrays.asList(86,87,88,89,90,91,92,93,94,95,96,97,98);
    private static Random random = new Random();

    /**
     * 对未达标的学生进行自动答题
     * @throws Exception
     */
    public void startTestPaper(Student student) throws Exception {
        long publicTimeConsuming = 0;
        long specialityTimeConsuming = 0;
        Document document;
        //如果基础课测试未超过2次 且 分数未达到45
        if(student.getPublicTestNum() < 2 && student.getPublicTestScore() < 48){
            long startTime = System.currentTimeMillis();
            document = getTestPaperHtml(PUBLIC_SUBJECT_URL, student.getIdentityId(), student.getName(), student.getSchoolId());
            startTestPaper(document, student, publicScoreList, student.getSchoolId());
            publicTimeConsuming = System.currentTimeMillis() - startTime;
        }
        //如果专业课测试未超过2次 且 分数未达到85
        if(student.getSpecialityTestNum() < 2 && student.getSpecialityTestScore() < 86){
            long startTime = System.currentTimeMillis();
            document = getTestPaperHtml(SPECIAL_SUBJECT_URL, student.getIdentityId(), student.getName(), student.getSchoolId());
            startTestPaper(document, student, specialityScoreList, student.getSchoolId());
            specialityTimeConsuming = System.currentTimeMillis() - startTime;
        }
        //获取考试结果 并更新数据库
        initTestPaperResult(student.getIdentityId(), student.getName(), student.getSchoolId());
        updateTimeConsuming(publicTimeConsuming, specialityTimeConsuming, student.getIdentityId());
    }

    private void updateTimeConsuming(Long publicTimeConsuming, Long specialityTimeConsuming, String identityId){
        Student student = studentMapper.selectByPrimaryKey(identityId);
        student.setIdentityId(identityId);
        student.setSpecialityTimeConsuming(specialityTimeConsuming.intValue()/60000);
        student.setPublicTimeConsuming(publicTimeConsuming.intValue()/60000);
        student.setIsAnswer(0);
        studentMapper.updateByPrimaryKeySelective(student);
    }

    /**
     * 获取学生答题结果，并更新在数据库中
     * @param identity  身份证
     * @param name  姓名
     * @throws Exception
     */
    public void initTestPaperResult(String identity, String name, Integer schoolId) throws Exception {
        HttpClient httpClient = new HttpClient();
        GetMethod getMethod = new GetMethod(QT_INFO_URL);
//        getMethod.setRequestHeader("Content-Type", "text/html; charset=utf-8");
        getMethod.setRequestHeader("cookie", toLogin(identity, name, schoolId));
        httpClient.executeMethod(getMethod);
        String result = getMethod.getResponseBodyAsString();
        Document document = Jsoup.parse(result);
        Elements elements = document.select("div div div table tr");
        Student record = new Student();
        record.setIdentityId(identity);
        log.info("[{}]Test paper result : {}", identity, elements.text());
        for(int i = 0; i < elements.size(); i++){
            //公共基础课
            if(i == 1){
                setScore(elements.get(i), record, "public");
            }
            //专业基础课
            if(i == 2){
                setScore(elements.get(i), record, "speciality");
            }
        }
        log.info("考试结果：{}", gson.toJson(record));
        studentMapper.updateByPrimaryKeySelective(record);
    }

    /**
     * 获取目标试卷HTML对象
     * @param testPaperUrl  试卷访问地址
     * @param identity  身份证
     * @param name  姓名
     * @return Document
     * @throws Exception
     */
    private Document getTestPaperHtml(String testPaperUrl, String identity, String name, Integer schoolId) throws Exception {
        String testPaperHtml = requestTestPaperHtml(testPaperUrl, identity, name, schoolId);
        return Jsoup.parse(testPaperHtml);
    }

    /**
     *
     * @param document  目标试卷HTML对象
     * @param student   学生信息
     * @param scoreList 分数区间
     * @throws Exception
     */
    private void startTestPaper(Document document, Student student, List<Integer> scoreList, Integer schoolId) throws Exception {
        //试卷提交路径
        String testPaperCommitPath = document.select("div div form").select("#myForm").attr("action");
        //试卷版本
        String testPaperType = getSubjectName(document);
        //根据试卷获取答案
        List<TestPaper> testPapers = testPaperMapper.selectByType(testPaperType);
        changeAnswers(testPapers, scoreList, testPaperType);
        log.info("{}-{}开始进入交卷等待...", student.getIdentityId(), student.getName());
        Thread.sleep(1000*60*7);
        //提交试卷
        submitAnswers(testPaperCommitPath, toLogin(student.getIdentityId(), student.getName(), schoolId), parseAnswersValue(testPapers));
        log.info("{}-{}提交试卷完成...", student.getIdentityId(), student.getName());
    }

    private void changeAnswers(List<TestPaper> testPapers, List<Integer> scoreList, String testPaperType){
        //随机获取考试分数 进行分数递减
        int targetScore = scoreList.get(random.nextInt(scoreList.size()));
        int maxScore = 98;
        int minScore = 85;
        if("大学语文A卷".equals(testPaperType) || "大学语文B卷".equals(testPaperType)){
            maxScore = 88;
            minScore = 46;
        }else if("大学语文C卷".equals(testPaperType)){
            maxScore = 76;
            minScore = 46;
        }
        List<String> list = new ArrayList<>();
        List<String> updateList = new ArrayList<>();
        int size = testPapers.size();
        while (true){
            TestPaper testPaper = testPapers.get(random.nextInt(size));
            if(updateList.contains(testPaper.getQuestionCode())){
                continue;
            }
            //大学语文A卷
            if("M31_104,M31_105,M31_106,M31_107,M31_108,M34_119,M34_120,M34_121,M34_122,M34_123".contains(testPaper.getQuestionCode())){
                continue;
            }
            //大学语文B卷
            if("M1833_5926,M1833_5927,M1833_5928,M1833_5929,M1833_5930,M1834_5931,M1834_5932,M1834_5933,M1834_5934,M1834_5935".contains(testPaper.getQuestionCode())){
                continue;
            }
            //大学语文C卷
            if("M1856_6021,M1856_6022,M1856_6023,M1856_6024,M1856_6025,M1858_6031,M1858_6032,M1858_6033,M1858_6034,M1858_6035,M1864_6061,M1864_6062,M1864_6063,M1864_6064,M1864_6065,M1865_6066,M1865_6067,M1865_6068,M1865_6069,M1865_6070"
                    .contains(testPaper.getQuestionCode())){
                continue;
            }
            if("Multiple".equals(testPaper.getQuestionType())){
                String[] array = testPaper.getQuestionCode().split("_");
                if(list.contains(array[0])){
                    continue;
                }
                list.add(array[0]);
            }
            int minusScore = maxScore - testPaper.getQuestionScore();
            if(minusScore == targetScore){
                maxScore = minusScore;
//                System.out.println(testPaperType + "-" + testPaper.getQuestionCode() + " >>>更改正确答案为错误答案" +
//                        testPaper.getRightAnswersCode() + " -> " + testPaper.getWrongAnswersCode() +
//                        " >>> 目标分数：" + targetScore + " >>> 扣减分数：" + testPaper.getQuestionScore() + " >>> 当前分数：" + maxScore);
                testPaper.setRightAnswersCode(testPaper.getWrongAnswersCode());
                return;
            }
            if(minusScore < minScore){
                return;
            }
            maxScore = minusScore;
//            System.out.println(testPaperType + "-" + testPaper.getQuestionCode() + " >>>更改正确答案为错误答案" +
//                    testPaper.getRightAnswersCode() + " -> " + testPaper.getWrongAnswersCode() +
//                    " >>> 目标分数：" + targetScore + " >>> 扣减分数：" + testPaper.getQuestionScore() + " >>> 当前分数：" + maxScore);
            testPaper.setRightAnswersCode(testPaper.getWrongAnswersCode());
            updateList.add(testPaper.getQuestionCode());
        }

    }

    /**
     * 试卷答案转换成HttpClient参数格式
     * @param testPapers    试卷答案
     * @return
     */
    private NameValuePair[] parseAnswersValue(List<TestPaper> testPapers) {
        NameValuePair[] answersParam = new NameValuePair[testPapers.size()];
        List<NameValuePair> answersList = new ArrayList<>();
        for(TestPaper testPaper : testPapers){
            answersList.add(new NameValuePair(testPaper.getQuestionCode(), testPaper.getRightAnswersCode()));
        }
        return answersList.toArray(answersParam);
    }

    /**
     * 将截取到的考试结果 赋值给 数据库实体对象
     * @param element   考试结果HTML对象
     * @param record    学生实体对象
     * @param type      类型 public：公共课，speciality：专业课
     */
    private void setScore(Element element, Student record, String type){
        Elements elements = element.select("td");
        int score = 0;
        int num = 0;
        for(int i = 0; i < elements.size(); i++){
            if(i == 1){
                if("您尚未进行测试".equals(elements.get(i).text())){
                    score = 0;
                    num = 0;
                }else{
                    String[] array = elements.get(i).text().split("，");
                    String regEx="[^0-9]";
                    Pattern p = Pattern.compile(regEx);
                    Matcher m = p.matcher(array[0]);
                    num = Integer.parseInt(m.replaceAll("").trim());
                    Matcher m2 = p.matcher(array[1]);
                    score = Integer.parseInt(m2.replaceAll("").trim());
                    if(type == "public"){
                        record.setPublicTestNum(num);
                        record.setPublicTestScore(score);
                    }
                    if(type == "speciality"){
                        record.setSpecialityTestNum(num);
                        record.setSpecialityTestScore(score);
                    }
                }
            }
        }
    }

    /**
     * 获取试卷版本名称
     * @param document  目标试卷HTML对象
     * @return
     */
    private static String getSubjectName(Document document){
        return document.select("div div table tr td h3").text();
    }

    /**
     * 提交试卷
     * @param urlMapping    试卷提交地址
     * @param cookie    cookie数据
     * @param answersParam  试卷答案
     * @throws Exception
     */
    private static void submitAnswers(String urlMapping, String cookie, NameValuePair[] answersParam) throws Exception {
        String reqUrl = String.format("%s%s", BASE_URL, urlMapping);
        HttpClient httpClient = new HttpClient();
        PostMethod postMethod = new PostMethod(reqUrl);
        postMethod.setRequestBody(answersParam);
        postMethod.setRequestHeader("cookie", cookie);
//        postMethod.setRequestHeader("Content-Type", "text/html; charset=utf-8");
        int statusCode = httpClient.executeMethod(postMethod);
        log.info(">>>测试题目提交请求，服务端返回Code：{}", statusCode);
    }


    private static String getTokenByCookie() throws Exception {
        HttpClient httpClient = new HttpClient();
        GetMethod getMethod = new GetMethod(QT_INFO_URL);
//        getMethod.setRequestHeader("Content-Type", "text/html; charset=utf-8");
        // 设置 HttpClient 接收 Cookie,用与浏览器一样的策略
        httpClient.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
        httpClient.executeMethod(getMethod);
        Cookie[] cookies = httpClient.getState().getCookies();
        for(Cookie c : cookies){
            String cookieStr = c.toString();
            if(!StringUtils.isEmpty(cookieStr) && cookieStr.contains(COOKIE_TOKEN_KEY)){
                return cookieStr;
            }
        }
        return "";
    }

    private static String getLoginCookie(String identity, String name, Integer schoolId) throws Exception {
        HttpClient httpClient = new HttpClient();
        PostMethod postMethod = new PostMethod(LOGIN_URL);
        //login method parameter
        NameValuePair[] data = {
                new NameValuePair("__RequestVerificationToken", "eCsShJuYuBT_oqaTzd4EH6gvmvhM7lWhdLWwd0VnikIC6K9UD57U9MY3Ka177NTa_7JzOXza3pmiCHuefMcI5-YY_9TKokpEQTEDIIY3tmQ1"),
                new NameValuePair("Type", "0"),
                new NameValuePair("Identity", identity),
                new NameValuePair("ConfirmIdentity", identity),
                new NameValuePair("Name", name),
                new NameValuePair("SpecialityId", "4"),
                new NameValuePair("SchoolId", String.valueOf(schoolId))
        };
        postMethod.setRequestBody(data);
        postMethod.setRequestHeader("Accept-Language", "zh-CN,zh;q=0.9");
        postMethod.setRequestHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3");
        postMethod.setRequestHeader("Accept-Encoding","gzip, deflate");
        postMethod.setRequestHeader("Cache-Control","max-age=0");
        postMethod.setRequestHeader("Connection","keep-alive");
        postMethod.setRequestHeader("Content-Type","application/x-www-form-urlencoded");
        postMethod.setRequestHeader("Host","opencourse.shtvu.org.cn");
        postMethod.setRequestHeader("Origin","http://opencourse.shtvu.org.cn");
        postMethod.setRequestHeader("Referer","http://opencourse.shtvu.org.cn/Qt/Home/Login");
        postMethod.setRequestHeader("User-Agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.142 Safari/537.36");
        // 设置 HttpClient 接收 Cookie,用与浏览器一样的策略
        httpClient.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
        postMethod.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "utf-8");
        int statusCode = httpClient.executeMethod(postMethod);
        // 获得登陆后的 Cookie
        Cookie[] cookies = httpClient.getState().getCookies();
        StringBuffer bufferCookie = new StringBuffer();
        for (Cookie c : cookies) {
            bufferCookie.append(c.toString() + ";");
        }
        //检测返回的cookie中是否包含有token，如果没有则通过其它请求进行获取
        if(!bufferCookie.toString().contains(COOKIE_TOKEN_KEY)){
            bufferCookie.append(getTokenByCookie());
        }
        if(statusCode == 302){
            String cookieStr = bufferCookie.toString();
            log.info(">>>模拟登录成功，获取到cookie值：{}", cookieStr);
            return cookieStr;
        }
        throw new RuntimeException(">>>模拟登录失败，请求中断...");
    }

    private static String toLogin(String identity, String name, Integer schoolId) throws Exception {
//        String cookie = personMap.get(identity);
//        if(StringUtils.isEmpty(cookie)){
//            cookie = getLoginCookie(identity, name, schoolId);
//            personMap.put(identity, cookie);
//        }
        return getLoginCookie(identity, name, schoolId);
    }


    private static String requestTestPaperHtml(String testPaperUrl, String identity, String name, Integer schoolId) throws Exception {
        HttpClient httpClient = new HttpClient();
        GetMethod getMethod = new GetMethod(testPaperUrl);
        getMethod.setRequestHeader("cookie", toLogin(identity, name, schoolId));
//        getMethod.setRequestHeader("Content-Type", "text/html; charset=utf-8");
        httpClient.executeMethod(getMethod);
        return getMethod.getResponseBodyAsString();
    }

}
