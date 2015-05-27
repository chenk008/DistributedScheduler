package org.distributedScheduler.biz.dataFetecher.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 用来标注任务是否单机运行
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface SingleRun {

}
