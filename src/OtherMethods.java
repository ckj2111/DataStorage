import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class OtherMethods {

	// write template.smali file
	public String writeTemplateFile(String directory, String className, String filePath, String trackerName) {
		try {
			// read file
			FileReader temp = new FileReader(filePath);
			BufferedReader br = new BufferedReader(temp);
			
			// write file
			File newFile = new File(directory + "/GetStoredData.smali");
			newFile.createNewFile();
			FileWriter fw = new FileWriter(newFile);
			BufferedWriter bw = new BufferedWriter(fw);
			
			// declare other variables
			String line = "";
			className = className.concat("GetStoredData;");
			Pattern p = Pattern.compile("\\.class");
			Pattern r = Pattern.compile("->writeToFile");
			
			
			while ((line = br.readLine()) != null) {
				Matcher a = p.matcher(line);
				Matcher c = r.matcher(line);
				if (a.find()) {
					line = ".class public " + className;
				}
				if (c.find()) {
					String part1 = (line.substring(0, line.lastIndexOf(",") + 1));
					String part2 = line.substring(line.indexOf(";") + 1);
					line = part1 + className + part2;
				}
				if (line != null) {
					if (line.contains("const-string") & line.contains("NEW")) {
						Pattern x = Pattern.compile(" v");
						Matcher o = x.matcher(line);
						if (o.find()) {
							line = line.substring(0, o.end() + 4) + trackerName + "\"";
						}
					}
				}
				
				
				// finally write line at the end of loop
				bw.write(line.concat("\n"));
				bw.flush();
				
			}
			br.close();
			bw.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return className;
	}
	
	// return register number
	public int returnReg(String line) {
		Pattern p = Pattern.compile(".locals ");
		Matcher m = p.matcher(line);
		
		int x = 0;
		
		if (m.find()) {
			x = Integer.parseInt(line.substring(m.end()));
		}
		
		return x;
	}

	// change register number
	public String changeReg(String line) {
		Pattern p = Pattern.compile(".locals ");
		Matcher m = p.matcher(line);
		
		String temp = "";
		
		if (m.find()) {
			int reg = Integer.parseInt(line.substring(m.end()));
			reg = reg + 3;
			temp = ".locals " + reg;
		}
		
		return temp;
	}
	
	// return variable number
	public String returnVar(String line) {
		Pattern p = Pattern.compile(" v");
		Pattern q = Pattern.compile(",");
		String returnv = "";
		
		Matcher m = p.matcher(line);
		Matcher n = q.matcher(line);
		if (m.find() & n.find()) {
			returnv = line.substring(m.start() + 1, n.start());
		}
		return returnv;
	}
	
	// check if Shared Preferences call is get/put
	public int checkGetPut(String line) {
		int i = 0;
		if (line.contains("getBoolean")) { i = 1; }
		else if (line.contains("getFloat")) { i = 2; }
		else if (line.contains("getInt")) { i = 3; }
		else if (line.contains("getLong")) { i = 4; }
		else if (line.contains("getStringSet")) { i = 5; }
		else if (line.contains("getString")) { i = 6; }
		else if (line.contains("putBoolean")) { i = 7; }
		else if (line.contains("putFloat")) { i = 8; }
		else if (line.contains("putInt")) { i = 9; }
		else if (line.contains("putLong")) { i = 10; }
		else if (line.contains("putStringSet")) { i = 11; }
		else if (line.contains("putString")) { i = 12; }
		
		return i;
			
	}
	
	// check new instances of class
	public String checkNewInstance(int localNum, String className) {
		String temp = "";
		temp = "new-instance v" + localNum + ", " + className + "\n\n";
		temp = temp.concat("invoke-direct {v" + localNum + "}, " + className + "-><init>()V\n\n");
		temp = temp.concat(".local v" + localNum  + ", \"t\":" + className + "\n");
		return temp;
	}
	
	// get values for Shared Preferences get/put
	public String getValues(String line, int x) {
		Pattern p = Pattern.compile("invoke-interface ");
		Pattern q = Pattern.compile("},");
		Matcher m = p.matcher(line);
		Matcher n = q.matcher(line);
		String temp = null;
		if (m.find() & n.find()) {
			// key 
			if (x == 2) {
				temp = line.substring(m.end() + 5, m.end() + 7);
			}
			// value
			else if (x == 3) {
				temp = line.substring(n.start() - 2, n.start());
			}
		}
		return temp;
	}
	
	// Shared Preferences: toString
	public String toSSharedPref(String key, String value, String className, int newReg, int callType, HashMap<String, String> map, boolean localChange) {
		String output = "";
		int reg2 = newReg + 1;
		int reg3 = newReg + 2;
			if (callType == 1 | callType == 7) { // Boolean
				output = output.concat("invoke-virtual {v" + newReg + ", " + key + ", " + value + "}, " + className + "->toS(Ljava/lang/String;Z)Ljava/lang/String;\n\n");
				output = output.concat("move-result-object v" + reg2 + "\n\n");
			} 
			else if (callType == 2 | callType == 8) { // float
				output = output.concat("invoke-virtual {v" + newReg + ", " + key + ", " + value + "}, " + className + "->toS(Ljava/lang/String;F)Ljava/lang/String;\n\n");
				output = output.concat("move-result-object v" + reg2 + "\n\n");
			}
			else if (callType == 3 | callType == 9) { // int
				output = output.concat("invoke-virtual {v" + newReg + ", " + key + ", " + value + "}, " + className + "->toS(Ljava/lang/String;I)Ljava/lang/String;\n\n");
				output = output.concat("move-result-object v" + reg2 + "\n\n");
			}
			else if (callType == 4 | callType == 10) { // long
				output = output.concat("invoke-virtual {v" + newReg + ", " + key + ", " + value + ", v" + reg3 + "}, " + className + "->toS(Ljava/lang/String;J)Ljava/lang/String;\n\n");
				output = output.concat("move-result-object v" + reg2 + "\n\n");
			} 
			else if (callType == 5 | callType == 11) { //Set<String>
				output = output.concat("invoke-virtual {v" + newReg + ", " + key + ", " + value + "}, " + className + "->toS(Ljava/lang/String;Ljava/util/Set;)Ljava/lang/String\n\n");
				output = output.concat("move-result-object v" + reg2 + "\n\n");
			}
			else if (callType == 6 | callType == 12) { // String
				output = output.concat("invoke-virtual {v" + newReg + ", " + key + ", " + value + "}, " + className + "->toS(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;\n\n");
				output = output.concat("move-result-object v" + reg2 + "\n\n");
			}
		return output;
		
	}

	// call WriteSharedPref
	public String writeShared(int newReg, String currentClass, String className) {
		String output = "";
		int reg2 = newReg + 1;
		int reg3 = newReg + 2;
		output = output.concat(".local v" + reg2 + ", \"temp\":Ljava/lang/String;\n\n");
		output = output.concat("invoke-virtual {p0}, " + currentClass + "->getApplicationContext()Landroid/content/Context;\n\n");
		output = output.concat("move-result-object v" + reg3 + "\n\n");
		output = output.concat("invoke-virtual {v" + newReg + ", v" + reg2 + ", v" + reg3 + "}, " + className + "->writeSharedPref(Ljava/lang/String;Landroid/content/Context;)V\n\n");
		
		return output;
	}
	
	// openFileOutput: get variables
	public String getVariables(String line) {
		String temp = "";
		Pattern p = Pattern.compile("invoke-virtual ");
		Matcher m = p.matcher(line);
		
		if (m.find()) {
			temp = line.substring(m.end() + 1, m.end() + 3) + " " + line.substring(m.end() + 5, m.end() + 7);
		}
		
		return temp;
	}

	// openFileOutput: call writeFileOutput
	public String writeFileOut(String variables, String className, String currentClass, int localNum) {
		String writeThis = variables.substring(3);
		int newReg = localNum + 1;
		String output = "";
		output = output.concat("invoke-virtual {p0}, " + currentClass + "->getApplicationContext()Landroid/content/Context;\n\n");
		output = output.concat("move-result-object v" + newReg + "\n\n");
		output = output.concat("invoke-virtual {v" + localNum + ", " + writeThis + ", v" + newReg + "}, " + className + "->writeFileOutput([BLandroid/content/Context;)V\n\n");
		
		return output;
	}

	// SQLite: get ContextValues
	public String getContextValues(String line) {
		Pattern p = Pattern.compile("invoke-virtual ");
		Matcher m = p.matcher(line);
		String output = "";
		if (m.find()) {
			output = line.substring(m.end() + 13, m.end() + 15);
		}
		
		return output;
	}
	
	// SQLite: write method
	public String writeSQLite(int localNum, String currentClass, String instanceName, String instanceClass, String contextValue, String className) {
		String output = "";
		int reg2 = localNum + 1;
		int reg3 = localNum + 2;
		output = output.concat("iget-object v" + reg2 + ", p0, " + currentClass + "->" + instanceName + ":" + instanceClass + "\n\n");
		output = output.concat("invoke-static {v" + reg2 + "}, " + instanceClass + "->access$0(" + instanceClass + ")Landroid/content/Context;\n\n");
		output = output.concat("move-result-object v" + reg2 + "\n\n");
		output = output.concat(".local v" + reg2 + ", \"context\":Landroid/content/Context;\n\n");
		output = output.concat("invoke-virtual {" + contextValue + "}, Landroid/content/ContentValues;->valueSet()Ljava/util/Set;\n\n");
		output = output.concat("move-result-object v" + reg3 + "\n\n");
		output = output.concat("invoke-virtual {v" + localNum + ", v" + reg3 + "}, " + className + "->toS(Ljava/util/Set;)Ljava/lang/String;\n\n");
		output = output.concat("move-result-object v" + reg3 + "\n\n");
		output = output.concat("invoke-virtual {v" + localNum + ", v" + reg3 + ", v" + reg2 + "}, " + className + "->writeSQLite(Ljava/lang/String;Landroid/content/Context;)V\n\n");
		
		return output;
	}
	
}
