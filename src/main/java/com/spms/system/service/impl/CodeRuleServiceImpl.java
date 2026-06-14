package com.spms.system.service.impl;

import com.spms.base.BaseService;
import com.spms.common.exception.AppException;
import com.spms.common.exception.CommonError;
import com.spms.system.entity.CodeRuleEntity;
import com.spms.system.enums.CodeRuleField;
import com.spms.system.enums.SerialNumberUpdate;
import com.spms.system.mapper.CodeRuleMapper;
import com.spms.system.service.CodeRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CodeRuleServiceImpl extends BaseService<CodeRuleEntity> implements CodeRuleService {
    private static final int DEFAULT_SN_LENGTH = 4;

    private final CodeRuleMapper codeRuleMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createCode(CodeRuleField field) {
        CodeRuleEntity rule = getOrCreateRule(field);
        LocalDateTime now = LocalDateTime.now();
        SerialNumberUpdate snType = SerialNumberUpdate.fromKey(rule.getSnType());
        int nextSn = shouldReset(rule, snType, now) ? 1 : safeCurrentSn(rule) + 1;

        rule.setCurrentSn(nextSn)
                .setCurrentYear(now.getYear())
                .setCurrentMonth(now.getMonthValue())
                .setCurrentDay(now.getDayOfMonth());
        codeRuleMapper.updateById(rule);

        return buildCode(rule, field, snType, nextSn, now);
    }

    private CodeRuleEntity getOrCreateRule(CodeRuleField field) {
        CodeRuleEntity rule = codeRuleMapper.selectByRuleFieldForUpdate(field.getKey());
        if (rule != null) {
            return rule;
        }
        CodeRuleEntity entity = new CodeRuleEntity();
        entity.setRuleField(field.getKey())
                .setPrefix(field.getDefaultPrefix())
                .setSnType(field.getDefaultSnType().getKey())
                .setTemplate(field.getDefaultSnType().getDefaultTemplate())
                .setSnLength(DEFAULT_SN_LENGTH)
                .setCurrentSn(0)
                .setCurrentYear(0)
                .setCurrentMonth(0)
                .setCurrentDay(0)
                .setIsSystem(true);
        codeRuleMapper.insert(entity);
        return codeRuleMapper.selectByRuleFieldForUpdate(field.getKey());
    }

    private boolean shouldReset(CodeRuleEntity rule, SerialNumberUpdate snType, LocalDateTime now) {
        return switch (snType) {
            case YEAR -> !Integer.valueOf(now.getYear()).equals(rule.getCurrentYear());
            case MONTH -> !Integer.valueOf(now.getYear()).equals(rule.getCurrentYear())
                    || !Integer.valueOf(now.getMonthValue()).equals(rule.getCurrentMonth());
            case DAY -> !Integer.valueOf(now.getYear()).equals(rule.getCurrentYear())
                    || !Integer.valueOf(now.getMonthValue()).equals(rule.getCurrentMonth())
                    || !Integer.valueOf(now.getDayOfMonth()).equals(rule.getCurrentDay());
            case NEVER -> false;
        };
    }

    private String buildCode(
            CodeRuleEntity rule,
            CodeRuleField field,
            SerialNumberUpdate snType,
            int serialNumber,
            LocalDateTime now
    ) {
        String prefix = rule.getPrefix() == null ? field.getDefaultPrefix() : rule.getPrefix();
        String template = rule.getTemplate() == null ? snType.getDefaultTemplate() : rule.getTemplate();
        int snLength = rule.getSnLength() == null || rule.getSnLength() < 1 ? DEFAULT_SN_LENGTH : rule.getSnLength();
        String serial = String.format("%0" + snLength + "d", serialNumber);
        return prefix + renderTemplate(template, now) + serial;
    }

    private String renderTemplate(String template, LocalDateTime now) {
        if (template == null) {
            return "";
        }
        return template
                .replace("yyyy", String.format("%04d", now.getYear()))
                .replace("yy", String.format("%02d", now.getYear() % 100))
                .replace("mm", String.format("%02d", now.getMonthValue()))
                .replace("dd", String.format("%02d", now.getDayOfMonth()))
                .replace("hh", String.format("%02d", now.getHour()));
    }

    private int safeCurrentSn(CodeRuleEntity rule) {
        Integer currentSn = rule.getCurrentSn();
        if (currentSn == null || currentSn < 0) {
            throw new AppException(CommonError.PARAM_INVALID, "编码规则流水号配置不正确");
        }
        return currentSn;
    }
}
