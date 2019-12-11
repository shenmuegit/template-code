package util;

public class StringUtil {

    /**
     * 根据type转换string为驼峰
     * @param str 字符
     * @param type 1:首字母大写否则小写
     * @return 转换后表名
     */
    public static String upperCaseOrLowerCaseTable(String str,int type){
        if(Empty4JUtil.stringIsEmpty(str)){
            return str;
        }
        int len;
        StringBuilder sbu = new StringBuilder(str);
        while (-1 != (len = sbu.indexOf("_"))){
            sbu = sbu.replace(len,len+2,sbu.substring(len+1,len+2).toUpperCase());
        }
        if(1 == type){
            return sbu.substring(0,1).toUpperCase() + sbu.substring(1);
        }else{
            return sbu.substring(0,1).toLowerCase() + sbu.substring(1);
        }
    }

    /**
     * 根据type转换string为驼峰
     * @param str 字符
     * @return 转换后表名
     */
    public static String tableNameToConstant(String str){
        return str.toUpperCase();
    }
}
