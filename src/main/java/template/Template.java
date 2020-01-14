package template;

import bean.Table;
import config.Config;
import dataSource.JdbcUtil;
import util.Empty4JUtil;
import util.FileUtil;
import util.StringUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

/**
 * @author bbcat
 */
public class Template {

    /**
     * 替换模板配置文件
     */
    private static final Map<String,String> TEMPLATE_KEY = new HashMap<>();

    /**
     * 模板保存路径
     */
    private static final Map<String,String> TEMPLATE_PATH = new HashMap<>();

    /**
     * 自定义模板
     */
    private static final Map<String,String> TEMPLATE = new HashMap<>();

    /**
     * 待填充bean模板
     */
    private static final Map<String,String> TEMPLATE_BEAN = new HashMap<>();

    private static final Map<String,String> TABLE_FIELD_JAVA = new HashMap<String,String>(){{
        put("bigint","Long");
        put("varchar","String");
        put("datetime","Date");
        put("int","Integer");
        put("double","Double");
        put("char","String");
        put("text","String");
    }};

    /**
     * 模板配置文件名
     */
    private static final String CONFIG_PATH = "templateCode/templateKey.properties";

    private static final Properties PROP = new Properties();

    private static final String TABLE = "table";

    private static final String LOWER_CASE_TABLE_NAME = "lowerCaseTableName";

    private static final String UPPER_CASE_TABLE_NAME = "upperCaseTableName";

    private static final String UPPER_LINE_TABLE_NAME = "underlineTableName";

    private static final String FUNCTION_NAME = "functionName";

    private static final String SERVICE_NAME = "serviceName";

    private static final String FILL = "_fill";

    private static final String IGNORE_TABLE_PREFIX = "ignoreTablePrefix";

    static{
        try {
            InputStreamReader in = new InputStreamReader(Config.class.getClassLoader().getResourceAsStream(CONFIG_PATH),"UTF-8");
            PROP.load(in);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void createTemplate() throws IOException {
        initTemplateKey();
        initTemplate();
        templateValueReplaceAndSave(TEMPLATE);
        templateValueReplaceAndSave(TEMPLATE_BEAN);
        updateTableToUpper(TEMPLATE_KEY.get(Template.TABLE));
    }

    private static void initTemplateKey() throws IOException {
        String table = PROP.getProperty(TABLE);
        if(Empty4JUtil.stringIsEmpty(table)){
            throw new IOException("table不可为空");
        }
        TEMPLATE_KEY.put(TABLE,table);

        Enumeration enume = PROP.propertyNames();
        while (enume.hasMoreElements()){
            String key = String.valueOf(enume.nextElement());
            if(Empty4JUtil.stringIsEmpty(key)){
                throw new IOException("templateKey.properties异常");
            }
            String value = judgeConstant(key);
            if(Empty4JUtil.stringIsNotEmpty(value)){
                TEMPLATE_KEY.put(key,value.trim());
                continue;
            }

            value = PROP.getProperty(key);
            if(Empty4JUtil.stringIsEmpty(value)){
                throw new IOException("自定义模板保存路径不可为空");
            }
            TEMPLATE_PATH.put(key,value.trim());
        }
    }

    /**
     * 生成模板
     */
    private static void initTemplate() {
        for (String key : TEMPLATE_PATH.keySet()) {
            InputStreamReader in = null;
            try {
                in = new InputStreamReader(Config.class.getClassLoader().getResourceAsStream("templateCode/" + key.trim().replace(FILL,"") + ".template"),"UTF-8");
                BufferedReader bufferedReader = new BufferedReader(in);
                String value;
                StringBuilder sbu = new StringBuilder();
                while ((value = bufferedReader.readLine()) != null) {
                    sbu.append(value).append("\n");
                }
                if(key.endsWith(FILL)){
                    TEMPLATE_BEAN.put(key,sbu.toString());
                }else{
                    TEMPLATE.put(key,sbu.toString());
                }
            }catch (Exception e){
                e.printStackTrace();
            } finally {
                if(null != in){
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private static String judgeConstant(String key) throws IOException {
        if(Empty4JUtil.stringIsEmpty(key)){
            throw new IOException("templateKey.properties异常");
        }
        String value = PROP.getProperty(key);
        if(Empty4JUtil.stringIsEmpty(value)){
            if(key.equals(LOWER_CASE_TABLE_NAME)){
                return StringUtil.upperCaseOrLowerCaseTable(TEMPLATE_KEY.get(TABLE),0);
            }
            if(key.equals(UPPER_CASE_TABLE_NAME)){
                return StringUtil.upperCaseOrLowerCaseTable(TEMPLATE_KEY.get(TABLE),1);
            }
            if(key.equals(UPPER_LINE_TABLE_NAME)){
                return StringUtil.tableNameToConstant(TEMPLATE_KEY.get(TABLE));
            }
            if(key.equals(FUNCTION_NAME)){
                return "default";
            }
        }else{
            /*拦截*/
            if(key.equals(LOWER_CASE_TABLE_NAME) ||
                    key.equals(UPPER_CASE_TABLE_NAME) ||
                    key.equals(FUNCTION_NAME) ||
                    key.equals(TABLE) ||
                    key.equals(SERVICE_NAME)){
                return value;
            }
            if(key.equals(IGNORE_TABLE_PREFIX)){
                return StringUtil.upperCaseOrLowerCaseTable(value,1);
            }
        }
        return null;
    }

    private static void templateValueReplaceAndSave(Map<String,String> template) throws IOException {
        if(Empty4JUtil.mapIsEmpty(template)){
            return;
        }
        for (Map.Entry<String, String> entry : template.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if(Empty4JUtil.stringIsEmpty(key) || Empty4JUtil.stringIsEmpty(value)){
                throw new IOException("自定义模板内容不可为空");
            }
            for(Map.Entry<String, String> templateKeyEntry : TEMPLATE_KEY.entrySet()){
                value = value.replace("{" + templateKeyEntry.getKey() + "}",templateKeyEntry.getValue());
            }
            if(key.endsWith(FILL)){
                value = fillVoDo(value);
            }
            value = value.replace(TEMPLATE_KEY.get(IGNORE_TABLE_PREFIX),"");
            save(key,value);
        }
    }

    private static String templatePathReplace(String path) throws IOException {
        if(Empty4JUtil.mapIsEmpty(TEMPLATE)){
            throw new IOException("自定义模板不可为空");
        }
        path = path.replace("{" + LOWER_CASE_TABLE_NAME + "}",TEMPLATE_KEY.get(LOWER_CASE_TABLE_NAME));
        path = path.replace("{" + UPPER_CASE_TABLE_NAME + "}",TEMPLATE_KEY.get(UPPER_CASE_TABLE_NAME));
        path = path.replace("{" + SERVICE_NAME + "}",TEMPLATE_KEY.get(SERVICE_NAME));
        return path;
    }

    private static void save(String key,String template) throws IOException {
        if(Empty4JUtil.stringIsEmpty(key) || Empty4JUtil.stringIsEmpty(template)){
            throw new IOException("自定义模板内容不可为空");
        }
        String savePath = TEMPLATE_PATH.get(key);
        if(Empty4JUtil.stringIsEmpty(savePath)){
            throw new IOException("自定义模板保存路径不可为空");
        }
        savePath = templatePathReplace(savePath);
        if(key.endsWith(FILL)){
            key = key.replace(FILL,"");
        }

        String fileName;
        if(key.equals("entity")){
            fileName = TEMPLATE_KEY.get(UPPER_CASE_TABLE_NAME);
        }else{
            fileName = TEMPLATE_KEY.get(UPPER_CASE_TABLE_NAME) + key.substring(0,1).toUpperCase() + key.substring(1);
        }
        FileUtil.createFile(
                fileName.replace(TEMPLATE_KEY.get(IGNORE_TABLE_PREFIX),""),
                Config.getSavePath() + savePath,
                template);
    }

    private static String fillVoDo(String value) throws IOException {
        List<Table> tables = JdbcUtil.getTables(Config.driver,Config.url,Config.username,Config.password,TEMPLATE_KEY.get(Template.TABLE));
        if(Empty4JUtil.listIsEmpty(tables)){
            throw new IOException(TEMPLATE_KEY.get(Template.TABLE) + ":不存在");
        }
        StringBuilder field = new StringBuilder();
        tables.forEach(s -> field.append("    /**\n     * ")
                .append(s.getColumnComment())
                .append("\n     */\n")
                .append("    private ")
                .append(TABLE_FIELD_JAVA.get(s.getDataType()))
                .append(" ")
                .append(StringUtil.upperCaseOrLowerCaseTable(s.getColumnName(),0))
                .append(";")
                .append("\n\n"));
        int location = value.indexOf("{");
        StringBuilder template = new StringBuilder(value);
        template.insert(location + 1,"\n\n" + field.toString());
        return template.toString();
    }

    private static void updateTableToUpper(String table){
        String sql = JdbcUtil.getCreateTableSql(Config.driver,Config.url,Config.username,Config.password,table);
        String[] sqls = sql.split("`");
        for (int i = 3; i < sqls.length-1; i = i+2) {
            sqls[i] = sqls[i].replaceAll("_","").toUpperCase();
        }
        JdbcUtil.delTable(Config.driver,Config.url,Config.username,Config.password,table);
        String tempSql = "";
        for (int i = 0; i < sqls.length; i++) {
            tempSql += sqls[i] + "`";
        }
        tempSql = tempSql.substring(0,tempSql.length()-1) + ";";
        JdbcUtil.runSql(Config.driver,Config.url,Config.username,Config.password,tempSql);
        /*将的*/
        System.out.println();
    }

}
