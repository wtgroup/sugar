package com.wtgroup.sugar.util;

import cn.hutool.core.util.StrUtil;
import com.wtgroup.sugar.consts.StringPool;
import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * hibernate validation 校验
 * <p>
 *
 * @author L&J
 * @date 2021/10/15 5:26 下午
 */
public class ValidUtil {

    private static Validator validator;
    static {
        ValidatorFactory validatorFactory = Validation.byProvider(HibernateValidator.class)
                .configure()
                .messageInterpolator(new ParameterMessageInterpolator()) // fix: javax.validation.ValidationException: HV000183: Unable to initialize 'javax.el.ExpressionFactory'. Check that you have the EL dependencies on the classpath, or use ParameterMessageInterpolator instead
                .buildValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    /**
     * 校验
     *
     * 失败后, 默认抛异常 RiskException
     * @param object
     * @param <T>
     */
    public static <T> void validate(T object) {
        validate(object, null);
    }

    /**
     * @param object
     * @param afterInvalid BiConsumer&lt;List&lt;String>, Set&lt;ConstraintViolation&lt;T>>>, p1: 提取好的间接消息, p2: 原始校验结果, 非空.
     * @param <T>
     */
    public static <T> void validate(T object, BiConsumer<List<String>, Set<ConstraintViolation<T>>> afterInvalid) {
        Set<ConstraintViolation<T>> validateResult = validator.validate(object);
        if (!validateResult.isEmpty()) {
            List<String> errors = getMessage(validateResult);
            if (afterInvalid != null) {
                afterInvalid.accept(errors, validateResult);
            } else {
                // 默认抛异常
                throw new IllegalArgumentException(StrUtil.join("; ", errors));
            }
        }
    }

    /**
     *
     * <pre>
     * interpolatedMessage: "不能为null", // getMessage() 可获取
     * rootBean: bean,
     * propertyPath: "字段路径",
     * messageTemplate: "{javax.validation.constraints.NotNull.message}", 以此为 key 换成本地换语言输出, 即 interpolatedMessage
     * rootBeanClass: 被校验的类 class
     *
     * </pre>
     *
     * @param validateResult
     * @param <T>
     * @return
     */
    public static <T> List<String> getMessage(Set<ConstraintViolation<T>> validateResult) {
        if (validateResult == null) {
            return Collections.emptyList();
        }
        return validateResult.stream().map(it -> {
            return "'" + it.getRootBeanClass().getSimpleName() + StringPool.DOT + it.getPropertyPath() + "' " + it.getMessage();
        }).collect(Collectors.toList());
    }


}
