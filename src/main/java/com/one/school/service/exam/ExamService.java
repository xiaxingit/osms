package com.one.school.service.exam;

import com.github.pagehelper.PageInfo;
import com.one.school.bean.StudentIdentityTemplate;
import com.one.school.dao.exam.ExamDetailMapper;
import com.one.school.dao.exam.ExamStudentMapper;
import com.one.school.entity.exam.ExamDetail;
import com.one.school.entity.exam.ExamStudent;
import com.one.school.util.ExcelUtil;
import com.one.school.util.FileUtil;
import com.one.school.util.OsmsUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by lee on 2019/8/17.
 */

@Slf4j
@Service
public class ExamService {

    @Value("${exam.template.file.path}")
    private String filePath;
    @Value("${exam.template.file.name}")
    private String fileName;
    @Value("${exam.identity.template.file.name}")
    private String IdentityFileName;

    @Resource
    private ExamDetailMapper examDetailMapper;
    @Resource
    private ExamStudentMapper examStudentMapper;

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    public boolean verStudent(String studentId, String studentPassword) {
        ExamStudent record = examStudentMapper.selectByPrimaryKey(studentId);
        if(null != record && studentPassword.equals(record.getStudentPassword())){
            return true;
        }
        return false;
    }

    public ExamStudent getExamInfoById(String studentId) {
        return examStudentMapper.selectStudentAndDetailByKey(studentId);
    }

    public PageInfo<ExamDetail> getDetailList(String studentId, String studentName, Integer pageNum, Integer pageSize) {
        OsmsUtil.defaultStartPage(pageNum, pageSize);
        List<ExamDetail> detailList = examDetailMapper.selectAll(studentId, studentName);
        return new PageInfo<>(detailList);
    }

    public PageInfo<ExamStudent> getIdentityDetailList(String studentId, String studentName, Integer pageNum, Integer pageSize) {
        OsmsUtil.defaultStartPage(pageNum, pageSize);
        List<ExamStudent> detailList = examStudentMapper.selectAll(studentId, studentName);
        return new PageInfo<>(detailList);
    }

    public void downloadTemplate(HttpServletResponse response) {
        FileUtil.download(response, filePath, fileName);
    }

    public void downloadIdentityTemplate(HttpServletResponse response) {
        FileUtil.download(response, filePath, IdentityFileName);
    }

    @Transactional
    public void updateData(MultipartFile excel, Integer userId) {
        Set<String> studentIds = new HashSet<>();
        Map<String, ExamDetail> map = new HashMap<>();
        List<ExamDetail> detailList = ExcelUtil.readExcel(excel, ExamDetail.class);
        for(ExamDetail detail : detailList){
            if(StringUtils.isEmpty(detail.getExamAddress())){
                log.warn("忽略学生考试信息 - 试卷号:{} , 学号:{}", detail.getTestPaperNumber(), detail.getStudentId());
                continue;
            }
            ExamDetail dbDetail = examDetailMapper.selectByTestPaperNumberAndStudentId(detail.getTestPaperNumber(), detail.getStudentId());

            if(null == dbDetail){
                studentIds.add(detail.getStudentId());
                detail.setCreateTime(new Date());
                detail.setCreateUserId(userId);
                detail.setExamDate(sdf.format(detail.getExcelExamDate()));
                examDetailMapper.insertSelective(detail);
            }else{
                detail.setId(dbDetail.getId());
                detail.setCreateTime(dbDetail.getCreateTime());
                detail.setCreateUserId(dbDetail.getCreateUserId());
                detail.setUpdateTime(new Date());
                detail.setUpdateUserId(userId);
                if(null != detail.getExcelExamDate()){
                    detail.setExamDate(sdf.format(detail.getExcelExamDate()));
                }
                examDetailMapper.updateByPrimaryKeySelective(detail);
            }
            map.put(detail.getStudentId(), detail);
        }
        for(String studentId : studentIds){
            ExamStudent record = examStudentMapper.selectByPrimaryKey(studentId);
            ExamDetail detail = map.get(studentId);
            if(null == detail){
                continue;
            }
            if(null == record){
                record = new ExamStudent();
                record.setStudentId(studentId);
                record.setStudentName(detail.getStudentName());
                record.setExamName(detail.getExamName());
                record.setGradeSeason(detail.getGradeSeason());
                record.setStudentCategory(detail.getStudentCategory());
                record.setMajor(detail.getMajor());
                record.setCreateTime(new Date());
                record.setCreateUserId(1);
                examStudentMapper.insertSelective(record);
            }else{
                record.setExamName(detail.getExamName());
                record.setGradeSeason(detail.getGradeSeason());
                record.setStudentCategory(detail.getStudentCategory());
                record.setMajor(detail.getMajor());
                examStudentMapper.updateByPrimaryKeySelective(record);
            }
        }
    }

    @Transactional
    public void uploadIdentityData(MultipartFile excel, Integer userId) {
        List<StudentIdentityTemplate> detailList = ExcelUtil.readExcel(excel, StudentIdentityTemplate.class);
        for(StudentIdentityTemplate data : detailList){
            ExamStudent record = examStudentMapper.selectByPrimaryKey(data.getStudentId());
            if(null == record) {
                record = new ExamStudent();
                record.setStudentId(data.getStudentId());
                record.setStudentName(data.getStudentName());
                record.setStudentPassword(data.getIdentityId());
                examStudentMapper.insertSelective(record);
            }else{
                record.setStudentName(data.getStudentName());
                record.setStudentPassword(data.getIdentityId());
                examStudentMapper.updateByPrimaryKeySelective(record);
            }
        }
    }

}
