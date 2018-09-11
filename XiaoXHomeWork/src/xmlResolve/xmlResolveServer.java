package xmlResolve;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSON;

public class xmlResolveServer extends HttpServlet {
	private static String FilePath = "C:/Users/admin/Desktop/XMLfolder/XMLfolder";

	public xmlResolveServer() {
		super();
	}

	public void destroy() {
		super.destroy(); // Just puts "destroy" string in log
		// Put your code here
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		synchronized (this) {// 单并发
			Map jMap = new HashMap();
			request.setCharacterEncoding("utf-8");
			response.setCharacterEncoding("utf-8");
			response.setContentType("text/html; charset=utf-8");
			String keyWords = request.getParameter("fileName").toUpperCase();

			String pattern = "^[A-Z0-9]+$";
			if (!keyWords.matches(pattern)) {
				jMap.put("success", "false");
				jMap.put("msg", "大小写字母加数字组合");
				response.getWriter().append(JSON.toJSONString(jMap));
				System.out.println(jMap);
				return;
			}

			List resList = new ArrayList();
			try {
				Map<String, String> fileMap = readfile(FilePath, keyWords + "_");
				for (String key : fileMap.keySet()) {
					String[] keysName = key.split("_");
					Map forMap = new HashMap();
					forMap.put("Lotnum", keysName[0]);
					forMap.put("Operation", keysName[1]);
					forMap.put("LotDecision", keysName[2]);
					forMap.put("Time",
							keysName[3].substring(0, keysName[3].length() - 4));
					if (keysName[2].equals("Quarantine")) {
						String fileMsg = fileMap.get(key);
						if (fileMsg.contains("ErrorMessage=")) {
							forMap.put("DataStatus", "ValidationFailed");
							int b = fileMsg.indexOf("ErrorMessage=\"") + 14;
							int e = fileMsg.indexOf(" final");
							String res = fileMsg.substring(b, e);
							forMap.put("O_total", res);
							int b2 = fileMsg.indexOf("do not match ") + 13;
							int e2 = fileMsg.indexOf(" in MES.");
							String res2 = fileMsg.substring(b2, e2);
							forMap.put("MES_total", res2);
						} else {
							forMap.put("DataStatus", "DataNotFound");
						}
					} else {
						forMap.put("DataStatus", "OK");
					}
					resList.add(forMap);

				}
				if (resList.size() == 0) {
					jMap.put("success", "false");
					jMap.put("msg", "无记录");
					response.getWriter().append(JSON.toJSONString(jMap));
					System.out.println(jMap);
					return;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			jMap.put("success", "true");
			jMap.put("resList", resList);
			System.out.println(jMap);
			response.getWriter().append(JSON.toJSONString(jMap));
		}
	}

	public void init() throws ServletException {
		// Put your code here
	}

	/**
	 * 读取文件夹下的所有文件
	 */
	private Map<String, String> readfile(String filepath, String targetName)
			throws Exception {
		Map resMap = new HashMap();
		try {
			File file = new File(filepath);
			String[] filelist = file.list();
			for (int i = 0; i < filelist.length; i++) {
				if (filelist[i].contains(targetName)) {
					File readfile = new File(filepath + "\\" + filelist[i]);
					if (!readfile.isDirectory()) {

						StringBuilder result = new StringBuilder();
						try {
							BufferedReader br = new BufferedReader(
									new FileReader(readfile.getPath()));
							String s = null;
							while ((s = br.readLine()) != null) {
								result.append(System.lineSeparator() + s);
							}
							br.close();
						} catch (Exception e) {
							e.printStackTrace();
						}
						resMap.put(readfile.getName(), result.toString());
					}
				}
			}
		} catch (Exception e) {
			System.out.println("readfile()   Exception:" + e.getMessage());
		}
		return resMap;
	}
}
