package com.tom.quiz.service.impl;

import com.tom.quiz.mapper.QuestionMapper;
import com.tom.quiz.mapper.QuestionSetMapper;
import com.tom.quiz.mapper.QuestionTypeMapper;
import com.tom.quiz.models.Question;
import com.tom.quiz.models.QuestionSet;
import com.tom.quiz.models.QuestionType;
import com.tom.quiz.service.QuestionService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.*;

@Service
public class QuestionServiceImpl implements QuestionService {

    @Autowired
    private QuestionMapper questionMapper;
    @Autowired
    private QuestionSetMapper questionSetMapper;
    @Autowired
    private QuestionTypeMapper questionTypeMapper;

    // ===== 添加题目 =====
    @Override
    @Transactional
    public Question addQuestion(Long teacherId, Question q) {
        checkOwnership(teacherId, q.getQuestionSetId());
        questionMapper.insert(q);
        updateSetCount(q.getQuestionSetId());
        return q;
    }

    // ===== 更新题目 =====
    @Override
    @Transactional
    public Question updateQuestion(Long teacherId, Question q) {
        Question existing = questionMapper.findById(q.getId());
        if (existing == null) throw new RuntimeException("题目不存在");
        checkOwnership(teacherId, existing.getQuestionSetId());
        q.setQuestionSetId(existing.getQuestionSetId());
        questionMapper.update(q);
        return q;
    }

    // ===== 删除题目 =====
    @Override
    @Transactional
    public void deleteQuestion(Long teacherId, Long questionId) {
        Question q = questionMapper.findById(questionId);
        if (q == null) return;
        checkOwnership(teacherId, q.getQuestionSetId());
        questionMapper.delete(questionId);
        updateSetCount(q.getQuestionSetId());
    }

    // ===== 查询 =====
    @Override
    public List<Question> getQuestionsBySetId(Long setId, String typeFilter, String difficulty) {
        if (typeFilter != null) {
            return questionMapper.findBySetIdAndType(setId, getTypeId(typeFilter));
        }
        if (difficulty != null) {
            return questionMapper.findBySetIdAndDifficulty(setId, difficulty);
        }
        return questionMapper.findBySetId(setId);
    }

    // ===== Excel 导入 =====
    @Override
    @Transactional
    public int importExcel(Long teacherId, Long setId, MultipartFile file) {
        checkOwnership(teacherId, setId);

        // 预加载题型映射
        Map<String, Integer> typeMap = new HashMap<>();
        for (QuestionType qt : questionTypeMapper.findAll()) {
            typeMap.put(qt.getTypeName(), qt.getId());
        }

        int count = 0;
        try (InputStream is = file.getInputStream(); Workbook wb = new XSSFWorkbook(is)) {
            Sheet sheet = wb.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) throw new RuntimeException("模板格式错误：缺少表头");

            // 解析表头列索引
            Map<String, Integer> colIdx = parseHeader(headerRow);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    Question q = rowToQuestion(row, colIdx, typeMap);
                    q.setQuestionSetId(setId);

                    // 去重检查
                    Question dup = questionMapper.findByContent(setId, q.getContent());
                    if (dup != null) continue;

                    questionMapper.insert(q);
                    count++;
                } catch (Exception e) {
                    // 该行错误，跳过，记录日志
                    System.err.println("Row " + (i + 1) + " import error: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Excel解析失败: " + e.getMessage());
        }

        updateSetCount(setId);
        return count;
    }

    // ===== Excel 导出 =====
    @Override
    public ByteArrayOutputStream exportExcel(Long setId) {
        List<Question> questions = questionMapper.findBySetId(setId);
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("题目列表");

            // 表头
            Row header = sheet.createRow(0);
            String[] headers = {"题型", "题目内容", "选项A", "选项B", "选项C", "选项D", "选项E", "选项F", "正确答案", "解析", "难度", "知识点标签"};
            for (int i = 0; i < headers.length; i++) {
                header.createCell(i).setCellValue(headers[i]);
            }

            // 数据
            for (int i = 0; i < questions.size(); i++) {
                Question q = questions.get(i);
                Row row = sheet.createRow(i + 1);
                QuestionType qt = questionTypeMapper.findById(q.getQuestionTypeId());
                row.createCell(0).setCellValue(qt != null ? qt.getTypeName() : "");
                row.createCell(1).setCellValue(stripHtml(q.getContent()));
                // 解析选项 JSON
                fillOptionCells(row, q.getOptions());
                row.createCell(8).setCellValue(q.getAnswer());
                row.createCell(9).setCellValue(stripHtml(q.getExplanation()));
                row.createCell(10).setCellValue(q.getDifficulty());
                row.createCell(11).setCellValue(q.getTags());
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            wb.write(bos);
            return bos;
        } catch (Exception e) {
            throw new RuntimeException("Excel生成失败: " + e.getMessage());
        }
    }

    // ===== 辅助方法 =====
    private void checkOwnership(Long teacherId, Long setId) {
        QuestionSet qs = questionSetMapper.findById(setId);
        if (qs == null || !qs.getTeacherId().equals(teacherId)) {
            throw new RuntimeException("无权操作该试题集");
        }
    }

    private void updateSetCount(Long setId) {
        int count = questionMapper.countBySetId(setId);
        QuestionSet qs = questionSetMapper.findById(setId);
        qs.setQuestionCount(count);
        questionSetMapper.update(qs);
    }

    private Integer getTypeId(String typeName) {
        QuestionType qt = questionTypeMapper.findByTypeName(typeName);
        return qt != null ? qt.getId() : null;
    }

    private Map<String, Integer> parseHeader(Row row) {
        Map<String, Integer> map = new LinkedHashMap<>();
        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null) {
                map.put(cell.getStringCellValue().trim(), i);
            }
        }
        return map;
    }

    private Question rowToQuestion(Row row, Map<String, Integer> col, Map<String, Integer> typeMap) {
        Question q = new Question();
        String typeName = getCellStr(row, col.get("题型"));
        Integer typeId = typeMap.get(typeName);
        if (typeId == null) throw new RuntimeException("未知题型: " + typeName);
        q.setQuestionTypeId(typeId);
        q.setContent(getCellStr(row, col.get("题目内容")));

        // 构建选项 JSON
        StringBuilder opts = new StringBuilder("[");
        String[] labels = {"A","B","C","D","E","F"};
        boolean first = true;
        for (int j = 0; j < 6; j++) {
            String val = getCellStr(row, col.get("选项" + labels[j]));
            if (val != null && !val.isEmpty()) {
                if (!first) opts.append(",");
                opts.append("{\"label\":\"").append(labels[j]).append("\",\"content\":\"").append(escapeJson(val)).append("\"}");
                first = false;
            }
        }
        opts.append("]");
        q.setOptions(opts.toString());
        q.setAnswer(getCellStr(row, col.get("正确答案")));
        q.setExplanation(getCellStr(row, col.get("解析")));
        q.setDifficulty(getCellStr(row, col.get("难度")));
        q.setTags(getCellStr(row, col.get("知识点标签")));
        q.setSortOrder(0);
        return q;
    }

    private void fillOptionCells(Row row, String optionsJson) {
        // 简单解析 JSON 数组填回各列
        if (optionsJson == null) return;
        String[] parts = optionsJson.replaceAll("[\\[\\]\"]", "").split("\\},\\{");
        for (int i = 0; i < parts.length && i < 6; i++) {
            String content = parts[i].replaceAll(".*\"content\":\"", "").replaceAll("\"$", "");
            row.createCell(2 + i).setCellValue(content);
        }
    }

    private String getCellStr(Row row, Integer idx) {
        if (idx == null) return null;
        Cell cell = row.getCell(idx);
        if (cell == null) return null;
        cell.setCellType(CellType.STRING);
        String val = cell.getStringCellValue();
        return (val == null || val.trim().isEmpty()) ? null : val.trim();
    }

    private String stripHtml(String html) {
        if (html == null) return null;
        return html.replaceAll("<[^>]+>", "");
    }

    private String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
