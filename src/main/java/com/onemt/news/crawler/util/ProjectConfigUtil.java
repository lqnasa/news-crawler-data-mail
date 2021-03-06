package com.onemt.news.crawler.util;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.configuration.ConfigurationException;  
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
/**
 * 
* @ClassName: ProjectConfigUtil
* @Description: 
* @author lennon dai
* @date 2016-12-4 下午4:21:39
*
 */
public class ProjectConfigUtil {  
  
    private static ProjectConfigUtil initor = new ProjectConfigUtil();     
      
    private static Map<String, Object> configMap = new HashMap<String, Object>();  
      
    private ProjectConfigUtil() {} 
    
    private static String configFile="config/project.properties";
      
    /** 
     * 获取内容 
     * @param configFile 
     * @param property 
     * @return 
     */  
    public static String get(String property) {     
        if(!configMap.containsKey(configFile)) {     
           initor.initConfig(configFile);  
        }  
        PropertiesConfiguration config = (PropertiesConfiguration) configMap.get(configFile);  
        String value = config.getString(property);  
        return value;     
    }     
      
    /** 
     * 载入配置文件，初始化后加入map 
     * @param configFile 
     */  
    private synchronized void initConfig(String configFile) {      
        try {  
            PropertiesConfiguration config = new PropertiesConfiguration(configFile);  
            config.setReloadingStrategy(new FileChangedReloadingStrategy());
            configMap.put(configFile, config);  
        } catch (ConfigurationException e) {  
            e.printStackTrace();  
        }  
    }  
    
}  