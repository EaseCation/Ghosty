package net.easecation.ghosty;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

/**
 * Created by boybook on 16/6/14.
 */
public class MathUtil {

    public static int getMax(int... nums) {
        Objects.requireNonNull(nums);
        boolean hasMax = false;
        int max = 0;
        for (int num: nums) {
            if (!hasMax || num > max) {
                max = num;
                hasMax = true;
            }
        }
        return max;
    }

    public static int getMin(int... nums) {
        Objects.requireNonNull(nums);
        boolean hasMin = false;
        int min = 0;
        for (int num: nums) {
            if (!hasMin || num < min) {
                min = num;
                hasMin = true;
            }
        }
        return min;
    }

    public static int getSecondTimestamp() {
        return getSecondTimestamp(System.currentTimeMillis());
    }

    /**
     * @return 精确到秒的时间戳
     */
    public static int getSecondTimestamp(long time) {
        String timestamp = String.valueOf(time);
        int length = timestamp.length();
        if (length > 3) {
            return Integer.valueOf(timestamp.substring(0,length-3));
        } else {
            return 0;
        }
    }

    public static String getTimestampString(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date(time));
    }

    public static String getSecondTimestampString(int sceondTimestamp) {
        return getTimestampString((long) sceondTimestamp * 1000L);
    }

    /**
     * 计算过两个时间差几天（以0点计算）
     * @param time1 第一个时间
     * @param time2 第二个时间
     * @return 相差几天
     */
    public static int getDateSpace(Date time1, Date time2) {
        Calendar calst = Calendar.getInstance();
        Calendar caled = Calendar.getInstance();

        calst.setTime(time1);
        caled.setTime(time2);

        //设置时间为0时
        calst.set(Calendar.HOUR_OF_DAY, 0);
        calst.set(Calendar.MINUTE, 0);
        calst.set(Calendar.SECOND, 0);
        caled.set(Calendar.HOUR_OF_DAY, 0);
        caled.set(Calendar.MINUTE, 0);
        caled.set(Calendar.SECOND, 0);
        //得到两个日期相差的天数

        return Math.abs(((int)(caled.getTime().getTime()/1000)-(int)(calst.getTime().getTime()/1000))/3600/24);
    }

    /**
     * 求给定双精度数组中值的和
     *
     * @param inputData
     *            输入数据数组
     * @return 运算结果
     */
    public static double getSum(double[] inputData) {
        if (inputData == null || inputData.length == 0)
            return -1;
        int len = inputData.length;
        double sum = 0;
        for (int i = 0; i < len; i++) {
            sum = sum + inputData[i];
        }

        return sum;

    }

    /**
     * 求给定双精度数组中值的数目
     *
     * @param inputData 输入数据数组
     * @return 运算结果
     */
    public static int getCount(double[] inputData) {
        if (inputData == null)
            return -1;

        return inputData.length;
    }

    /**
     * 求给定双精度数组中值的平均值
     *
     * @param inputData
     *            输入数据数组
     * @return 运算结果
     */
    public static double getAverage(double[] inputData) {
        if (inputData == null || inputData.length == 0)
            return -1;
        int len = inputData.length;
        double result;
        result = getSum(inputData) / len;

        return result;
    }

    /**
     * 求给定双精度数组中值的平方和
     *
     * @param inputData
     *            输入数据数组
     * @return 运算结果
     */
    public static double getSquareSum(double[] inputData) {
        if(inputData==null||inputData.length==0)
            return -1;
        int len=inputData.length;
        double sqrsum = 0.0;
        for (int i = 0; i <len; i++) {
            sqrsum = sqrsum + inputData[i] * inputData[i];
        }


        return sqrsum;
    }

    /**
     * 求给定双精度数组中值的方差
     *
     * @param inputData
     *            输入数据数组
     * @return 运算结果
     */
    public static double getVariance(double[] inputData) {
        int count = getCount(inputData);
        double sqrsum = getSquareSum(inputData);
        double average = getAverage(inputData);
        double result;
        result = (sqrsum - count * average * average) / count;

        return result;
    }

    /**
     * 求给定双精度数组中值的标准差
     *
     * @param inputData
     *            输入数据数组
     * @return 运算结果
     */
    public static double getStandardDiviation(double[] inputData) {
        double result;
        //绝对值化很重要
        result = Math.sqrt(Math.abs(getVariance(inputData)));

        return result;

    }

}
