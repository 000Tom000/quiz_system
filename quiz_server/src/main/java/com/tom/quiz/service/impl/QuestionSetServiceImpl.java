package com.tom.quiz.service.impl;

import com.tom.quiz.mapper.QuestionSetClassMapper;
import com.tom.quiz.mapper.QuestionSetMapper;
import com.tom.quiz.models.QuestionSet;
import com.tom.quiz.service.QuestionSetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class QuestionSetServiceImpl implements QuestionSetService {

    @Autowired
    private QuestionSetMapper qsMapper;
    @Autowired
    private QuestionSetClassMapper qscMapper;

    @Override
    @Transactional
    public QuestionSet create(Long teacherId, QuestionSet qs, List<String> classNames) {
        qs.setTeacherId(teacherId);
        qsMapper.insert(qs);
        if (classNames != null && !classNames.isEmpty()) {
            qscMapper.batchInsert(qs.getId(), classNames);
        }
        return qs;
    }

    @Override
    @Transactional
    public QuestionSet update(Long teacherId, QuestionSet qs, List<String> classNames) {
        QuestionSet existing = qsMapper.findById(qs.getId());
        if (existing == null || !existing.getTeacherId().equals(teacherId)) {
            throw new RuntimeException("无权操作或试题集不存在");
        }
        qs.setTeacherId(teacherId);
        qsMapper.update(qs);
        if (classNames != null) {
            qscMapper.deleteBySetId(qs.getId());
            if (!classNames.isEmpty()) {
                qscMapper.batchInsert(qs.getId(), classNames);
            }
        }
        return qs;
    }

    @Override
    @Transactional
    public void delete(Long teacherId, Long setId) {
        QuestionSet existing = qsMapper.findById(setId);
        if (existing == null || !existing.getTeacherId().equals(teacherId)) {
            throw new RuntimeException("无权操作或试题集不存在");
        }
        qscMapper.deleteBySetId(setId);
        qsMapper.delete(setId, teacherId);
    }

    @Override
    public QuestionSet getById(Long setId) {
        return qsMapper.findById(setId);
    }

    @Override
    public List<QuestionSet> getMySets(Long teacherId) {
        return qsMapper.findByTeacherId(teacherId);
    }

    @Override
    public List<QuestionSet> getVisibleSets(String className) {
        return qsMapper.findVisibleByClass(className);
    }

    @Override
    public List<QuestionSet> search(String className, String keyword) {
        return qsMapper.search(className, keyword);
    }
}
