package com.shxy.smartlearningacademycommon.utils;

/**
 * @author huang qi long
 * @version 1.0
 * @School Suihua University
 * @since 2026/4/7 12:47
 */
public class BaseContext {

    private static final ThreadLocal<Long> threadLocal=new ThreadLocal<>();


    /**
     * 设置当前线程的userId
     * @param userId
     */
    public static void setThreadLocal(Long userId){
        threadLocal.set(userId);
    }

    /**
     * 获取当前线程的userId
     * @return
     */
    public static Long getThreadLocal(){
        return threadLocal.get();
    }


    /**
     * 清理当前线程的userId
     */
    public static void removeThreadLocal(){
        threadLocal.remove();
    }


}
