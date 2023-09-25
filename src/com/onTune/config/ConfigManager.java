package com.onTune.config;

import com.onTune.Log.onTuneLog;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.ini4j.Profile;
import org.ini4j.Wini;

public class ConfigManager {
  public final String CONF_FILE = "Config.ini";
  
  private String SMSCharSet;
  
  private String ResultCharSet;
  
  private String SMSURL;
  
  private String SMSParam;
  
  private String LMSParam;
  
  private String JSonTitle;
  
  private int MsgLength;
  
  private int MsgLengthType;
  
  private int Base64Used;
  
  private int ArrayType;
  
  private int ConnTimeout;
  
  private int ReadTimeout;
  
  private Boolean IsPost;
  
  private boolean DebugLogMode;
  
  private Map<String, String> HeaderMap;
  
  private List<String> ParamValArray;
  
  private List<String> ParamKeyArray;
  
  public static ConfigManager getInstance() {
    return Singleton.instance;
  }
  
  private static class Singleton {
    private static final ConfigManager instance = new ConfigManager();
  }
  
  private ConfigManager() {
    this.SMSCharSet = "UTF-8";
    this.ResultCharSet = "UTF-8";
    this.SMSURL = "";
    this.SMSParam = "SM";
    this.LMSParam = "LM";
    this.JSonTitle = "";
    this.MsgLength = 90;
    this.MsgLengthType = 0;
    this.Base64Used = 0;
    this.ArrayType = 1;
    this.ConnTimeout = 10;
    this.ReadTimeout = 10;
    this.IsPost = Boolean.valueOf(true);
    this.DebugLogMode = false;
    this.HeaderMap = new HashMap<String, String>();
    this.ParamValArray = new ArrayList<String>();
    this.ParamKeyArray = new ArrayList<String>();
  }
  
  public boolean isDebugLogMode() {
    return this.DebugLogMode;
  }
  
  public String getSMSURL() {
    return this.SMSURL;
  }
  
  public String getSMSParam() {
    return this.SMSParam;
  }
  
  public String getLMSParam() {
    return this.LMSParam;
  }
  
  public String getSMSCharSet() {
    return this.SMSCharSet;
  }
  
  public String getResultCharSet() {
    return this.ResultCharSet;
  }
  
  public int getMsgLength() {
    return this.MsgLength;
  }
  
  public int getMsgLengthType() {
    return this.MsgLengthType;
  }
  
  public int getConnTimeout() {
    return this.ConnTimeout;
  }
  
  public int getReadTimeout() {
    return this.ReadTimeout;
  }
  
  public Map<String, String> getHeaderMap() {
    return this.HeaderMap;
  }
  
  public List<String> getParamValArray() {
    return this.ParamValArray;
  }
  
  public List<String> getParamKeyArray() {
    return this.ParamKeyArray;
  }
  
  public Boolean isPost() {
    return this.IsPost;
  }
  
  public String getJSonTitle() {
    return this.JSonTitle;
  }
  
  public int getBase64Used() {
    return this.Base64Used;
  }
  
  public int getArrayType() {
    return this.ArrayType;
  }
  
  public boolean LoadConfig(String SMSargs[]) {
    StringBuilder sbFilePath = new StringBuilder(System.getProperty("user.dir"));
    sbFilePath.append("\\config\\config.ini");
    File fIni = new File(sbFilePath.toString());
    if (!fIni.isFile()) {
      try {
        Path path = Paths.get(sbFilePath.toString(), new String[0]);
        File fDir = new File(path.getParent().toString());
        if (!fDir.exists())
          fDir.mkdirs(); 
        FileWriter fw = new FileWriter(fIni, true);
        fw.write("[SMS]\r\nSMSURL = \r\nCharSet = UTF8\r\nMsgLength = 1000\r\nMsgLengType = 1\r\nPost = 1\r\nbase64 = 0\r\nArrayType = 1\r\n\r\n\r\n[SmsType]\r\nSMS = SM\r\nLMS = LM\r\n\r\n\r\n[Header]\r\nContent-Type = application/json\r\ncharset = UTF8\r\n\r\n\r\n[Parameter]\r\nstrSenderID=DHLEE\r\nstrSenderName=onTune\r\nstrReceiverIDs=ontuneargs0\r\nstrContext=ontunemsgs1\r\nbase64=true\r\ntextType=1\r\nmessageClass=1\r\n\r\n\r\n[Result]\r\nCharSet = UTF8\r\n\r\n\r\n[encoding]\r\ntarget=strContext");
        BufferedWriter out = new BufferedWriter(fw);
        out.newLine();
        fw.flush();
        out.close();
        fw.close();
      } catch (IOException e) {
        onTuneLog.logger.error("CreateConfig", e);
      } 
    } else {
      try {
        BufferedReader in = new BufferedReader(new FileReader(sbFilePath.toString()));
        int nParameter = 0;
        String sLine;
        while ((sLine = in.readLine()) != null) {
          if (sLine.trim().toLowerCase().equals("[parameter]")) {
            nParameter++;
            continue;
          } 
          if (nParameter != 1 || 
            sLine.length() <= 1 || 
            sLine.trim().substring(0, 1).equals("#"))
            continue; 
          if (sLine.trim().substring(0, 1).equals("["))
            break; 
          int Index = sLine.indexOf("=");
          if (Index > 0) {
            this.ParamKeyArray.add(sLine.substring(0, Index).trim());
            this.ParamValArray.add(sLine.substring(Index + 1).trim());
          } 
        } 
        in.close();
      } catch (IOException e) {
        onTuneLog.logger.error("LoadParam", e);
        System.exit(1);
      } 
    } 
    boolean retBool = true;
    Wini ini = null;
    try {
      ini = new Wini();
      ini.getConfig().setFileEncoding(Charset.forName("euc-kr"));
      ini.getConfig().setLowerCaseOption(true);
      ini.getConfig().setLowerCaseSection(true);
      ini.setFile(new File(sbFilePath.toString()));
      ini.load();
      
      Profile.Section sectSmsType = (Profile.Section)ini.get("smstype");
      String smsType = null;
      if(sectSmsType.get("type") != null) {
    	  if(sectSmsType.get("type", "telegram") != null) {
    		  smsType = sectSmsType.get("type", "telegram").trim();
    	  };
    	  if(sectSmsType.get("type", "teams") != null) {
    		  smsType = sectSmsType.get("type", "teams").trim();
    	  };
      }
      
      Profile.Section sect = (Profile.Section)ini.get("sms");
      if (sect != null) {
    	if(smsType != null) {
    		if("telegram".equals(smsType)) {
    			String[] ontuneArgs = SMSargs[0].split("/");
				SMSargs[0] = ontuneArgs[1];
				SMSURL = sect.get("smsurl", SMSURL).replace("{token}", ontuneArgs[0]).trim();
    		} else if("teams".equals(smsType)) {
    			String[] ontuneArgs = SMSargs[0].split("/");
    			String urlParam = ontuneArgs[0].concat("@").concat(ontuneArgs[1]).concat("/IncomingWebhook/").concat(ontuneArgs[2]).concat("/").concat(ontuneArgs[3]);
    			SMSURL = sect.get("smsurl", this.SMSURL).replace("{param}", "").trim().concat(urlParam);
    		}
    	} else {
    		this.SMSURL = sect.get("smsurl", this.SMSURL).trim();
    	}
        this.SMSCharSet = sect.get("charset", this.SMSCharSet).trim();
        this.MsgLength = Integer.parseInt(sect.get("msglength", "90"));
        this.MsgLengthType = Integer.parseInt(sect.get("msglengthtype", "0"));
        this.ConnTimeout = Integer.parseInt(sect.get("conntimeout", "10"));
        this.ReadTimeout = Integer.parseInt(sect.get("readtimeout", "10"));
        this.Base64Used = Integer.parseInt(sect.get("base64", "0"));
        this.ArrayType = Integer.parseInt(sect.get("arraytype", "1"));
        this.JSonTitle = sect.get("jsontitle", this.JSonTitle).trim();
        this.IsPost = Boolean.valueOf((Integer.parseInt(sect.get("post", "1")) == 1));
        this.DebugLogMode = (((Integer)sect.get("debuglog", int.class, Integer.valueOf(0))).intValue() > 0);
      } else {
        retBool = false;
        onTuneLog.logger.error("Config.ini", "No Section[SMS]");
      } 
      sect = (Profile.Section)ini.get("smstype");
      if (sect != null) {
        this.SMSParam = sect.get("sms", this.SMSParam).trim();
        this.LMSParam = sect.get("lms", this.LMSParam).trim();
      } else {
        onTuneLog.logger.info("Config.ini", "No Section[SmsType]");
      } 
      sect = (Profile.Section)ini.get("result");
      if (sect != null) {
        this.ResultCharSet = sect.get("charset", this.ResultCharSet).trim();
      } else {
        retBool = false;
        onTuneLog.logger.error("Config.ini", "No Section[Result]");
      } 
      Profile.Section hSections = (Profile.Section)ini.get("header");
      Iterator<String> hKeySet = hSections.keySet().iterator();
      while (hKeySet.hasNext()) {
        String key = hKeySet.next();
        this.HeaderMap.put(key, hSections.get(key));
      } 
    } catch (Exception e) {
      onTuneLog.logger.error("LoadConfig", e);
    } finally {
      ini = null;
    } 
    return retBool;
  }
}
