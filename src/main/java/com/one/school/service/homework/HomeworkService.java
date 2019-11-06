package com.one.school.service.homework;

import com.github.pagehelper.PageInfo;
import com.one.school.dao.homework.HomeworkCoursewareMapper;
import com.one.school.dao.homework.HomeworkStudentMapper;
import com.one.school.entity.homework.HomeworkCourseware;
import com.one.school.entity.homework.HomeworkStudent;
import com.one.school.util.OsmsUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by lee on 2019/11/3.
 */

@Slf4j
@Service
public class HomeworkService {

    @Value("${homework.chrome.drive.path}")
    private String drivePath;

    @Resource
    private HomeworkStudentMapper homeworkStudentMapper;
    @Resource
    private HomeworkCoursewareMapper homeworkCoursewareMapper;

    public PageInfo<HomeworkStudent> getStudentList(Integer pageNum, Integer pageSize, Integer status) {
        OsmsUtil.defaultStartPage(pageNum, pageSize);
        List<HomeworkStudent> list = homeworkStudentMapper.selectAll(status);
        return new PageInfo<>(list);
    }

    public List<HomeworkCourseware> getCoursewareList(String studentId) {
        return homeworkCoursewareMapper.selectByStudentId(studentId);
    }

    public HomeworkStudent insertStudentInfo(String studentId, String studentName) {
        HomeworkStudent record = homeworkStudentMapper.selectByStudentId(studentId);
        if(null == record){
            record = new HomeworkStudent();
            record.setStudentId(studentId);
            record.setStudentName(studentName);
            record.setCreateTime(new Date());
            record.setStatus(0);
            homeworkStudentMapper.insertSelective(record);
        }
        return record;
    }

    public boolean updateCoursewareInfo(String studentId, String courseName, String courseType) {
        courseName = StringUtils.isNotEmpty(courseName) ? courseName.trim() : courseName;
        HomeworkCourseware courseware = homeworkCoursewareMapper.selectByStudentIdAndCourseName(studentId, courseName);
        if(null == courseware){
            courseware = new HomeworkCourseware();
            courseware.setCoursewareName(courseName);
            courseware.setCoursewareType(courseType);
            courseware.setStudentId(studentId);
            courseware.setIsDone(0);
            homeworkCoursewareMapper.insertSelective(courseware);
            return false;
        }
        return 1 == courseware.getIsDone();
    }

    private void updateStatus(String studentId, String coursewareName, Integer isDone) {
        coursewareName = StringUtils.isNotEmpty(coursewareName) ? coursewareName.trim() : coursewareName;
        homeworkCoursewareMapper.updateStatus(studentId, coursewareName, isDone);
    }

    public void handleHomework(String studentIds) {
        if(StringUtils.isEmpty(studentIds)){
            return;
        }
        String[] array = studentIds.split(",");
        if(null == array || array.length == 0){
            return;
        }
        homeworkStudentMapper.updateStatus(array);
        for(String studentId : array){
            HomeworkStudent student = homeworkStudentMapper.selectByStudentId(studentId);
            handleHomework(student.getStudentId(), student.getPwd(), 2);
        }
    }

    public void handleHomework(String studentId, String password, Integer isSpare) {
        System.setProperty("webdriver.chrome.driver", drivePath);
        WebDriver driver = new ChromeDriver();
        HomeworkStudent student = null;
        try {
            //主站
            if(1 == isSpare){
                goLoginPage(driver);
                String loginPage = findNewWindow(driver);
                switchPage(driver, loginPage, true);
                inputAccount(driver, studentId, password);

                student = insertStudentInfo(studentId, null);

                goCoursePage(driver);
                String coursePage = findNewWindow(driver);
                switchPage(driver, coursePage, true);
            }
            //备用网站
            if(2 == isSpare){
                student = insertStudentInfo(studentId, null);
                goLoginPageBySpare(driver);
                inputAccountBySpare(driver, studentId, password);
            }
            //
            List<WebElement> pptElements = new ArrayList<>();
            List<WebElement> videoElements = new ArrayList<>();
            List<WebElement> pictureElements = new ArrayList<>();
            List<WebElement> unknownElements = new ArrayList<>();
            String courseDetailWindow = driver.getWindowHandle();
            for(int i = 1; i < getCourseElements(driver); i++){
                WebElement element = driver.findElement(By.xpath("//*[@id=\"rightBox\"]/div[6]/table/tbody/tr["+ (i + 1) +"]/td[1]/div/div/div/a"));
                boolean isOtherPage = goCourseDetailPage(driver, element, courseDetailWindow);
                if(isOtherPage){
                    continue;
                }
                openVideoList(driver, pptElements, videoElements, pictureElements, unknownElements);
                operationPicture(driver, pptElements, courseDetailWindow, studentId);
                operationVideo(driver, videoElements, courseDetailWindow, studentId);
                operationPpt(driver, pptElements, courseDetailWindow, studentId);
                operationOther(unknownElements);
                pptElements.clear();
                videoElements.clear();
                unknownElements.clear();
                Thread.sleep(5000);
                driver.navigate().back();
            }
            student.setStatus(1);
            homeworkStudentMapper.updateInfo(student);
        }catch (Exception ex) {
            log.error("", ex);
            student.setStatus(-1);
            student.setMsg(ex.getMessage());
            homeworkStudentMapper.updateInfo(student);
        } finally {
            driver.quit();
        }
    }

    private void goLoginPageBySpare(WebDriver driver) throws InterruptedException {
        driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
        driver.get("http://learning.shou.org.cn/");
    }

    private void goLoginPage(WebDriver driver) throws InterruptedException {
        driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
        driver.get("http://www.sou.edu.cn/");
        Thread.sleep(3000);
        driver.findElement(By.xpath("//a[text()='师生统一身份认证入口']")).click();
    }

    private void inputAccount(WebDriver driver, String userName, String userPwd) throws InterruptedException {
        Thread.sleep(3000);
        driver.findElement(By.xpath("//input[@id='username']")).sendKeys(userName);
        Thread.sleep(3000);
        driver.findElement(By.xpath("//input[@id='password']")).sendKeys(userPwd);
        Thread.sleep(3000);
        driver.findElement(By.xpath("//input[@id='signIn']")).click();
    }

    private void inputAccountBySpare(WebDriver driver, String userName, String userPwd) throws InterruptedException {
        Thread.sleep(3000);
        driver.findElement(By.xpath("//*[@id=\"UserName\"]")).sendKeys(userName);
        Thread.sleep(3000);
        driver.findElement(By.xpath("//*[@id=\"Password\"]")).sendKeys(userPwd);
        Thread.sleep(3000);
        driver.findElement(By.xpath("//*[@id=\"loginbutton\"]")).click();
    }

    private void switchPage(WebDriver driver, String window, boolean closeCurrentPage) {
        if(closeCurrentPage){
            driver.close();
        }
        driver.switchTo().window(window);
    }

    private String findNewWindow(WebDriver driver) {
        //当前页面的窗口值
        String currentWindow = driver.getWindowHandle();
        for(String handle : driver.getWindowHandles()){
            if(!currentWindow.equals(handle)){
                return handle;
            }
        }
        return null;
    }


    private void goCoursePage(WebDriver driver) throws InterruptedException {
        Thread.sleep(3000);
        driver.findElement(By.xpath("//*[@id=\"allThings\"]/div[1]/div/div[2]/div/a")).click();
    }

    private int getCourseElements(WebDriver driver) {
        List<WebElement> trList = driver.findElements(By.xpath("//*[@id=\"rightBox\"]/div[6]/table/tbody//tr"));
        int size = null == trList || trList.isEmpty() ? 0 : trList.size();
        return size;
    }

    private boolean goCourseDetailPage(WebDriver driver, WebElement element, String courseDetailWindow) throws InterruptedException {
        Thread.sleep(3000);
        element.click();
        for(String handle : driver.getWindowHandles()){
            if(!courseDetailWindow.equals(handle)){
                driver.switchTo().window(handle);
                driver.close();
                driver.switchTo().window(courseDetailWindow);
                return true;
            }
        }
        return false;
    }

    private void openVideoList(WebDriver driver, List<WebElement> pptElements, List<WebElement> videoElements,
                               List<WebElement> pictureElements, List<WebElement> unknownElements) throws InterruptedException {
        List<WebElement> webElementList = driver.findElements(By.className("sh-toc-act"));
        List<WebElement> topicElements = new ArrayList<>();
        for(WebElement webElement : webElementList){
            if(StringUtils.isNotEmpty(webElement.getText()) && webElement.getText().contains("必看")){
                List<WebElement> topicList = webElement.findElements(By.xpath(".//a"));
                for(WebElement e : topicList){
                    topicElements.add(e);
                }
            }
        }
        if(null == topicElements || topicElements.isEmpty()){
            return;
        }
        System.out.println(">>> 开始点开详细课程。。。");
        for(WebElement e : topicElements){
            e.click();
            Thread.sleep(3000);
        }

        List<WebElement> elements = driver.findElements(By.className("sh-res-h"));
        for(WebElement e : elements){
            String text = e.getText();
            if(!e.isDisplayed() || StringUtils.isEmpty(text) || !text.contains("必看")){
                continue;
            }
            WebElement a = e.findElement(By.xpath("./a[1]"));
            if(text.contains(".avi") || text.contains(".mp4") || text.contains("视频")
                    || text.contains("如何进行人力资源的供给预测")){
                videoElements.add(a);
            }else if(text.contains("ppt") || text.contains("office") || text.contains("PDF")){
                pptElements.add(a);
            }else if(text.contains("图片") || text.contains("公众号")) {
                pictureElements.add(a);
            }else{
                unknownElements.add(a);
            }
        }
    }

    private void operationPpt(WebDriver driver, List<WebElement> pptElements, String courseDetailWindow, String studentId) throws InterruptedException {
        for(WebElement e : pptElements){
            String text = e.getText();
            if(updateCoursewareInfo(studentId, text, "文档")){
                continue;
            }
            e.click();
            Thread.sleep(3000);
            String newWindow = findNewWindow(driver);
            switchPage(driver, newWindow, false);
            seeOffice(driver);
            updateStatus(studentId, text, 1);
            switchPage(driver, courseDetailWindow, true);
        }
    }

    private void operationVideo(WebDriver driver, List<WebElement> videoElements, String courseDetailWindow, String studentId) throws InterruptedException {
        for(WebElement e : videoElements){
            String text = e.getText();
            if(updateCoursewareInfo(studentId, text, "视频")){
                continue;
            }
            e.click();
            Thread.sleep(3000);
            String newWindow = findNewWindow(driver);
            switchPage(driver, newWindow, false);
            playVideo(driver);
            updateStatus(studentId, text, 1);
            switchPage(driver, courseDetailWindow, true);
        }
    }

    private void operationPicture(WebDriver driver, List<WebElement> pptElements, String courseDetailWindow, String studentId) throws InterruptedException {
        for(WebElement e : pptElements){
            String text = e.getText();
            if(updateCoursewareInfo(studentId, text, "图片")){
                continue;
            }
            e.click();
            Thread.sleep(3000);
            String newWindow = findNewWindow(driver);
            switchPage(driver, newWindow, false);
            Thread.sleep(1000);
            updateStatus(studentId, text, 1);
            switchPage(driver, courseDetailWindow, true);
        }
    }

    private void operationOther(List<WebElement> unknownElements) {
        for(WebElement e : unknownElements){
            System.out.println(">>> 未知类型：" + e.getTagName() + " >>> " + e.getText());
        }
    }

    private void playVideo(WebDriver driver) throws InterruptedException {
        Thread.sleep(30000);
        WebElement videoPlayer = driver.findElement(By.tagName("video"));
        if(null == videoPlayer){
            driver.navigate().refresh();
            Thread.sleep(60000);
            videoPlayer = driver.findElement(By.tagName("video"));
        }
        JavascriptExecutor js = (JavascriptExecutor) driver;
        Double videoDuration;
        do{
            videoDuration = (Double) js.executeScript("return arguments[0].duration", videoPlayer);
            if(null == videoDuration){
                Thread.sleep(5000);
            }
        }while (null == videoDuration);
        waitVideo(js, videoPlayer, videoDuration);
    }

    private void seeOffice(WebDriver driver) throws InterruptedException {
        Thread.sleep(10000);
        String totalNumStr = driver.findElement(By.xpath("//*[@id=\"doc_box\"]/div/div[4]/div/span/em")).getText();
        int totalNum = Integer.parseInt(totalNumStr);
        WebElement nextElement = driver.findElement(By.xpath("//*[@id=\"doc_box\"]/div/div[4]/div/a[2]"));
        for(int i = 0; i < totalNum; i++){
            Thread.sleep(3000);
            nextElement.click();
        }
    }

    private void waitVideo(JavascriptExecutor js, WebElement videoPlayer, Double totalDuration) throws InterruptedException {
        Boolean paused = (Boolean) js.executeScript("return arguments[0].paused", videoPlayer);
        if(paused){
            js.executeScript("return arguments[0].play()", videoPlayer);
        }
        boolean ended;
        do {
            Object ObjectTime = js.executeScript("return arguments[0].currentTime", videoPlayer);
            int currentTime = 0;
            if(ObjectTime instanceof Double){
                currentTime = ((Double) ObjectTime).intValue();
            }else if(ObjectTime instanceof Long){
                currentTime = Integer.parseInt(String.valueOf(ObjectTime));
            }
            int surplusTime = totalDuration.intValue() - currentTime;
            if(surplusTime >= 0){
                Thread.sleep((surplusTime * 1000 + 3000));
            }
            ended = (Boolean) js.executeScript("return arguments[0].ended", videoPlayer);
        }while (!ended);
    }

}
