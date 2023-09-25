package com.onTune.SMS;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import com.onTune.Log.onTuneLog;
import com.onTune.config.ConfigManager;
import com.onTune.logger.LogManager;
import com.onTune.logger.onTuneLogManager;



public class onTuneEventSmsHttpSender {
			
	public static void main(String[] args) {
	    SmsSender SMS = new SmsSender(args);
	    SMS.SendSMSMessage(args);
	    onTuneLog.logger.debug("App terminated.");
	    System.exit(0);
	}			
}

class SmsParamRec {
  public String Key = "";
  
  public String Value = "";
}


class SmsSender {
	  private String Parameters = "";
	  
	  private Boolean IsLMSParam = Boolean.valueOf(false);
	  
	  private Boolean IsJSonType = Boolean.valueOf(false);
	  
	  private SmsParamRec MsgParamRec = new SmsParamRec();
	  
	  private String[] SMSArgs;
	  
	  private ConfigManager ConfMgr;
	  
	  public SmsSender(String[] smsargs) {
	    this.SMSArgs = smsargs;
	    this.ConfMgr = ConfigManager.getInstance();
	    onTuneLog.logger.getLogMgr().setFilePath(System.getProperty("user.dir") + "/log/onTuneSMS");
	    onTuneLog.logger.getLogMgr().setMaxLogMegabytes(50);
	    onTuneLog.logger.getLogMgr().setBakRollingCount(10);
	  }
	  
	  private void LoadConfig(String SMSargs[]) {
//		  LogManager.IsConsoleLog = true; // 개발 로그
	    this.ConfMgr.LoadConfig(SMSargs);
	    onTuneLogManager.IsDebugLog = this.ConfMgr.isDebugLogMode();
	    if (this.ConfMgr.isDebugLogMode()) {
	      onTuneLog.logger.debug("[Version] v1.0.0.6");
	      onTuneLog.logger.debug("[URL] " + this.ConfMgr.getSMSURL());
	      onTuneLog.logger.debug("[SMS Param] " + this.ConfMgr.getSMSParam());
	      onTuneLog.logger.debug("[LMS Param] " + this.ConfMgr.getLMSParam());
	      onTuneLog.logger.debug("[CharSet] " + this.ConfMgr.getSMSCharSet());
	      onTuneLog.logger.debug("[ResultCharSet] " + this.ConfMgr.getResultCharSet());
	      onTuneLog.logger.debug("[MSG_LENGTH] " + this.ConfMgr.getMsgLength());
	      onTuneLog.logger.debug("[MSG_LENGTH_TYPE] " + this.ConfMgr.getMsgLengthType());
	      onTuneLog.logger.debug("[CONN_TIMEOUT] " + this.ConfMgr.getConnTimeout());
	      onTuneLog.logger.debug("[READ_TIMEOUT] " + this.ConfMgr.getReadTimeout());
	      onTuneLog.logger.debug("[base64Used] " + this.ConfMgr.getBase64Used());
	      onTuneLog.logger.debug("[ArrayType] " + this.ConfMgr.getArrayType());
	      onTuneLog.logger.debug("[JSonTitle] " + this.ConfMgr.getJSonTitle());
	      if (this.ConfMgr.isPost().booleanValue()) {
	        onTuneLog.logger.debug("[Method] POST");
	      } else {
	        onTuneLog.logger.debug("[Method] GET");
	      } 
	      if (this.ConfMgr.isDebugLogMode()) {
	        List<String> pKeyArray = this.ConfMgr.getParamKeyArray();
	        List<String> pValArray = this.ConfMgr.getParamValArray();
	        for (int j = 0; j < pKeyArray.size(); j++)
	          onTuneLog.logger.debug("Params[" + j + "] " + (String)pKeyArray.get(j) + "=" + (String)pValArray.get(j)); 
	      } 
	      int iIndex = 0;
	      Iterator<String> hKeySet = this.ConfMgr.getHeaderMap().keySet().iterator();
	      while (hKeySet.hasNext()) {
	        String key = hKeySet.next();
	        onTuneLog.logger.debug("Header[" + iIndex + "] " + key + " = " + (String)this.ConfMgr.getHeaderMap().get(key));
	        iIndex++;
	      } 
	      for (int i = 0; i < this.SMSArgs.length; i++)
	        onTuneLog.logger.debug("Args[" + String.valueOf(i) + "] " + this.SMSArgs[i]); 
	    } 
	  }
	  
	  private Boolean LoadOnTuneMsgs(String sKey, String sVal, String[] args) {
	    ConfigManager ConfMgr = ConfigManager.getInstance();
	    if (sVal.length() > 10) {
	      String sTmp = sVal.substring(0, 10).toLowerCase();
	      if (sTmp.equals("ontunemsgs")) {
	        int index = Integer.parseInt(sVal.substring(10));
	        sVal = args[index];
	        int nLength = 0;
	        if (ConfMgr.getMsgLengthType() == 0) {
	          nLength = GetBytesLength(sVal);
	          if (nLength > ConfMgr.getMsgLength()) {
	            this.IsLMSParam = Boolean.valueOf(true);
	            onTuneLog.logger.warn("[MSG-" + nLength + " bytes -> " + ConfMgr.getMsgLength() + "] ORGMSG: " + sVal);
	            sVal = strCut(sVal, null, ConfMgr.getMsgLength(), 0, true, false);
	          } 
	        } else if (ConfMgr.getMsgLengthType() == 1) {
	          nLength = sVal.length();
	          if (nLength > ConfMgr.getMsgLength()) {
	            this.IsLMSParam = Boolean.valueOf(true);
	            onTuneLog.logger.warn("[MSG-" + nLength + " Count -> " + ConfMgr.getMsgLength() + "] ORGMSG: " + sVal);
	            sVal = sVal.substring(0, ConfMgr.getMsgLength());
	          } 
	        } 
	        this.MsgParamRec.Key = sKey;
	        if (ConfMgr.getBase64Used() == 1) {
	          this.MsgParamRec.Value = Base64.getEncoder().encodeToString(sVal.getBytes());
	        } else {
	          this.MsgParamRec.Value = sVal;
	        } 
	        return Boolean.valueOf(true);
	      } 
	    } 
	    return Boolean.valueOf(false);
	  }
	  
	  public int GetBytesLength(String str) {
	    int nCount = 0;
	    try {
	      nCount = (str.getBytes("euc-kr")).length;
	    } catch (UnsupportedEncodingException e) {
	      onTuneLog.logger.error(e);
	    } 
	    return nCount;
	  }
	  
	  public String strCut(String szText, String szKey, int nLength, int nPrev, boolean isNotag, boolean isAdddot) {
	    String r_val = szText;
	    int oF = 0, oL = 0, rF = 0, rL = 0;
	    int nLengthPrev = 0;
	    Pattern p = Pattern.compile("<(/?)([^<>]*)?>", 2);
	    if (isNotag)
	      r_val = p.matcher(r_val).replaceAll(""); 
	    r_val = r_val.replaceAll("&amp;", "&");
	    r_val = r_val.replaceAll("(!/|\r|\n|&nbsp;)", "");
	    try {
	      byte[] bytes = r_val.getBytes("UTF-8");
	      if (szKey != null && !szKey.equals("")) {
	        nLengthPrev = (r_val.indexOf(szKey) == -1) ? 0 : r_val.indexOf(szKey);
	        nLengthPrev = (r_val.substring(0, nLengthPrev).getBytes("MS949")).length;
	        nLengthPrev = (nLengthPrev - nPrev >= 0) ? (nLengthPrev - nPrev) : 0;
	      } 
	      int j = 0;
	      if (nLengthPrev > 0)
	        while (j < bytes.length) {
	          if ((bytes[j] & 0x80) != 0) {
	            oF += 2;
	            rF += 3;
	            if (oF + 2 > nLengthPrev)
	              break; 
	            j += 3;
	            continue;
	          } 
	          if (oF + 1 > nLengthPrev)
	            break; 
	          oF++;
	          rF++;
	          j++;
	        }  
	      j = rF;
	      while (j < bytes.length) {
	        if ((bytes[j] & 0x80) != 0) {
	          if (oL + 2 > nLength)
	            break; 
	          oL += 2;
	          rL += 3;
	          j += 3;
	          continue;
	        } 
	        if (oL + 1 > nLength)
	          break; 
	        oL++;
	        rL++;
	        j++;
	      } 
	      r_val = new String(bytes, rF, rL, "UTF-8");
	      if (isAdddot && rF + rL + 3 <= bytes.length)
	        r_val = r_val + "..."; 
	    } catch (UnsupportedEncodingException e) {
	      onTuneLog.logger.error(e);
	    } 
	    return r_val;
	  }
	  
	  private void AddParameterValue(String sKey, String sVal, String[] args) {
	    ConfigManager ConfMgr = ConfigManager.getInstance();
	    try {
	      if (sVal.length() > 10) {
	        String sTmp = sVal.substring(0, 10).toLowerCase();
	        if (sTmp.equals("ontuneargs")) {
	          int index = Integer.parseInt(sVal.substring(10));
	          sVal = args[index];
	          int Index = sVal.indexOf("&");
	          if (Index > 0) {
	            String[] sValues = sVal.split("&");
	            for (int iCnt = 0; iCnt < sValues.length; iCnt++) {
	              if (iCnt == 0) {
	                sVal = sValues[0];
	              } else {
	                Index = sValues[iCnt].indexOf("=");
	                if (Index > 0) {
	                  String[] sKeyVal = sValues[iCnt].split("=");
	                  sKey = sKeyVal[0];
	                  sVal = sKeyVal[1];
	                } 
	              } 
	              AddParameterValue(sKey, sVal);
	            } 
	          } else {
	            AddParameterValue(sKey, sVal);
	          } 
	        } else if (sTmp.equals("ontunemsgs")) {
	          AddParameterValue(this.MsgParamRec.Key, this.MsgParamRec.Value);
	        } else if (sTmp.equals("ontunedate")) {
	          Date now = new Date();
	          SimpleDateFormat format = new SimpleDateFormat(sVal.substring(10));
	          sVal = format.format(now);
	          AddParameterValue(sKey, sVal);
	        } else if (sTmp.equals("ontunetype")) {
	          if (this.IsLMSParam.booleanValue()) {
	            sVal = ConfMgr.getLMSParam();
	          } else {
	            sVal = ConfMgr.getSMSParam();
	          } 
	          AddParameterValue(sKey, sVal);
	        } else {
	          AddParameterValue(sKey, sVal);
	        } 
	      } else {
	        AddParameterValue(sKey, sVal);
	      } 
	    } catch (Exception e) {
	      onTuneLog.logger.error("[ key: " + sKey + " val: " + sVal + "]", e);
	    } 
	  }
	  
	  private void AddParameterValue(String sKey, String sVal) {
	    try {
	      if (this.IsJSonType.booleanValue()) {
	        if (this.Parameters.equals("")) {
	          this.Parameters = "\"" + sKey + "\":\"" + sVal + "\"";
	        } else {
	          this.Parameters += ",\"" + sKey + "\":\"" + sVal + "\"";
	        } 
	      } else {
	        if (this.Parameters.equals("")) {
	          this.Parameters = URLEncoder.encode(sKey, this.ConfMgr.getSMSCharSet()) + "=" + URLEncoder.encode(sVal, this.ConfMgr.getSMSCharSet());
	        } else {
	          this.Parameters += "&" + URLEncoder.encode(sKey, this.ConfMgr.getSMSCharSet()) + "=" + URLEncoder.encode(sVal, this.ConfMgr.getSMSCharSet());
	        } 
	        onTuneLog.logger.debug("[" + this.ConfMgr.getSMSCharSet() + "] " + sVal + " >>>>> " + URLEncoder.encode(sVal, this.ConfMgr.getSMSCharSet()));
	      } 
	    } catch (UnsupportedEncodingException e) {
	      onTuneLog.logger.error("[ key: " + sKey + " val: " + sVal + "]", e);
	    } 
	  }
	  
	  private void LoadParameters() {
	    this.IsLMSParam = Boolean.valueOf(false);
	    for (int i = 0; i < this.ConfMgr.getParamKeyArray().size() && 
	      !LoadOnTuneMsgs(this.ConfMgr.getParamKeyArray().get(i), this.ConfMgr
	        .getParamValArray().get(i), this.SMSArgs).booleanValue(); i++);
	    onTuneLog.logger.debug("[MSG] " + this.MsgParamRec.Key + "= " + this.MsgParamRec.Value);
	    String contType = (String)this.ConfMgr.getHeaderMap().get("content-type");
	    if (contType != null && !contType.isEmpty()) {
	      String[] arrContType = contType.split("/");
	      this.IsJSonType = Boolean.valueOf((arrContType[arrContType.length - 1].toLowerCase().indexOf("json") >= 0));
	    } 
	    for (int j = 0; j < this.ConfMgr.getParamKeyArray().size(); j++)
	      AddParameterValue(this.ConfMgr.getParamKeyArray().get(j), this.ConfMgr.getParamValArray().get(j), this.SMSArgs); 
	    if (this.IsJSonType.booleanValue())
	      if (!this.ConfMgr.getJSonTitle().isEmpty()) {
	        this.Parameters = "{\"" + this.ConfMgr.getJSonTitle() + "\" : [{" + this.Parameters + "}]}";
	      } else if (this.ConfMgr.getArrayType() == 0) {
	        this.Parameters = "{" + this.Parameters + "}";
	      } else {
	        this.Parameters = "[{" + this.Parameters + "}]";
	      }  
	    onTuneLog.logger.debug("[URL] " + this.ConfMgr.getSMSURL() + " [PARAMS] " + this.Parameters);
	  }
	  
	  private URLConnection OpenURL(URL url) throws IOException {
	    URLConnection urlconn = url.openConnection();
	    urlconn.setConnectTimeout(this.ConfMgr.getConnTimeout() * 1000);
	    urlconn.setReadTimeout(this.ConfMgr.getReadTimeout() * 1000);
	    urlconn.setUseCaches(false);
	    Iterator<String> keySetIterator = this.ConfMgr.getHeaderMap().keySet().iterator();
	    while (keySetIterator.hasNext()) {
	      String key = ((String)keySetIterator.next()).trim();
	      urlconn.setRequestProperty(key, ((String)this.ConfMgr.getHeaderMap().get(key)).trim());
	      onTuneLog.logger.debug("[SET_HEADER] " + key + " = " + ((String)this.ConfMgr.getHeaderMap().get(key)).trim());
	    } 
	    urlconn.setDoOutput(this.ConfMgr.isPost().booleanValue());
	    return urlconn;
	  }
	  
	  private void ResponseURL(URLConnection urlconn) throws UnsupportedEncodingException, IOException {
	    onTuneLog.logger.debug("[RESULT] Start. ");
	    BufferedReader rd = null;
	    rd = new BufferedReader(new InputStreamReader(urlconn.getInputStream(), this.ConfMgr.getResultCharSet()));
	    String line = null;
	    StringBuilder sbRet = new StringBuilder();
	    while ((line = rd.readLine()) != null) {
	      sbRet.append(line);
	      onTuneLog.logger.debug("[RESULT] " + line);
	    } 
	    onTuneLog.logger.info("[RESULT] " + sbRet.toString());
	    onTuneLog.logger.debug("[RESULT] End. ");
	    rd.close();
	  }
	  
	  private void SendSMSPost() throws UnsupportedEncodingException, IOException {
	    URL url = new URL(this.ConfMgr.getSMSURL());
	    URLConnection con = OpenURL(url);
	    LoadParameters();
	    OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
	    System.out.println(this.Parameters);
	    wr.write(this.Parameters);
	    wr.flush();
	    ResponseURL(con);
	  }
	  
	  private void SendSMS() throws IOException {
	    LoadParameters();
	    URL url = new URL(this.ConfMgr.getSMSURL() + "?" + this.Parameters);
	    URLConnection con = OpenURL(url);
	    ResponseURL(con);
	  }
	  
	  public void SendSMSMessage(String SMSargs[]) {
	    LoadConfig(SMSargs);
	    if (this.ConfMgr.getSMSURL().equals("")) {
	      this.ConfMgr.getClass();
	      onTuneLog.logger.error("Config.ini", "No Section ident : [SMS] SMSURL = ?");
	    } else if (this.SMSArgs.length > 1) {
	      try {
	        if (this.ConfMgr.isPost().booleanValue()) {
	          SendSMSPost();
	        } else {
	          SendSMS();
	        } 
	      } catch (Exception e) {
	        onTuneLog.logger.error(e);
	        onTuneLog.logger.error("[URL] " + this.ConfMgr.getSMSURL() + " [PARAMS] " + this.Parameters);
	      } 
	    } else {
	      onTuneLog.logger.error("A variable number of arguments less. argumentCount[" + this.SMSArgs.length + "]");
	    } 
	  }
	}




//"Hana_TEST_Log_single" "oel7-2-ERROR TEST....3 ERROR"  "01039110511_�씠愿묓썕&user=123456769_�솉湲몃룞"