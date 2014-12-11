import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class main {


	// main class
	public static void main(String[] args) {

		main m = new main();
		String temp = "";
		if (args[2].lastIndexOf("/") == (args[2].length() + 1)) {
			temp = args[2].substring(0, args[2].length() - 1);
		}
		else {
			temp = args[2];
		}
		String keytoolLoc = temp.concat("dist");
		String fileName = keytoolLoc.concat(args[1].substring(args[1].lastIndexOf("/")));
		m.runTime("java -jar " + args[0] + " d " + args[1] + " -o " + args[2] + " -f");
		m.parseFileTree(args[2], args[3], args[4]);
		m.runTime("java -jar " + args[0] + " b " + args[2]);
		m.runTime("cd " + args[5]);
		List<String> command = Arrays.asList("keytool", "-genkey", "-v", "-keystore", keytoolLoc.concat("/c.keystore"), "-alias", "aaa", "-keyalg", "RSA", "-keysize", "2048", "-validity", "10000", "-storepass", "password");
		m.signApk(command);
		command = Arrays.asList("jarsigner", "-verbose", "-keystore", keytoolLoc.concat("/c.keystore"), "-storepass", "password", fileName, "aaa");
		m.signApk(command);
		
	} // end main

	// runtime command
	public void runTime(String command) {
		System.out.println(command);
		try {
			Process p = Runtime.getRuntime().exec(command);
			BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line;
			while ((line = input.readLine()) != null) {
				System.out.println(line);
				if (line.contains("Building apk file")) {
					System.out.println("Success!");
				}
			}
			input.close();
			p.waitFor();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	// keytool & jarsigner runtime
	public void signApk(List<String> command) {
		System.out.println(command);
		ProcessBuilder pb = new ProcessBuilder(command);
		pb.redirectErrorStream(true);

		Process p = null;
		
		try {
			p = pb.start();
		} catch (IOException e) {
			e.printStackTrace();
		}

		InputStream out = p.getInputStream();
		OutputStream in = p.getOutputStream();

		byte[] buffer = new byte[4000];



		try {
			while (p.isAlive()) {
				if (out.available() > 0) {
					out.read(buffer);
					System.out.print(new String(buffer));
					buffer = new byte[4000];
				}
				if (System.in.available() > 0) {
					System.in.read(buffer);
					System.out.print(new String(buffer));
					in.write(buffer);
					in.flush();
					buffer = new byte[4000];
				}
			}

			out.close();
			in.close();
		} catch (IOException e) {

		}
	}
	
	// walk fileTree
	public void parseFileTree(final String directory, final String trackName, final String templatePath) {

		Path dir = Paths.get(directory);

		try {
			Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {

				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attr) {

					// if file is a .smali file
					if (attr.isRegularFile() & file.toString().contains(".smali")) {

						// declare necessary variables
						HashMap <String, Boolean> storage = new HashMap<String, Boolean>();
						HashMap <String, String> varStorage = new HashMap<String, String>();
						boolean findMatch = false, changeLocals = false;
						String methodName = "", className = "", currentClass = "";

						// --------------------------
						// STEP 1:
						// check for relevant strings
						// --------------------------

						try {
							FileReader temp = new FileReader(file.toString());
							BufferedReader br = new BufferedReader(temp);
							String line = "";
							Pattern p = Pattern.compile("SharedPreferences;->get");
							Pattern v = Pattern.compile("SharedPreferences$Editor;->put");
							Pattern o = Pattern.compile("openFileOutput");
							Pattern q = Pattern.compile("SQLiteDatabase;->insert");
							Pattern u = Pattern.compile("SQLiteOpenHelper");
							Pattern r = Pattern.compile("\\.method");
							Pattern s = Pattern.compile("\\.end");
							Pattern t = Pattern.compile("\\.class ");


							while ((line = br.readLine()) != null) {
								Matcher a = p.matcher(line);
								Matcher b = o.matcher(line);
								Matcher c = q.matcher(line);
								Matcher d = r.matcher(line);
								Matcher e = s.matcher(line);
								Matcher f = t.matcher(line);
								Matcher g = u.matcher(line);
								Matcher h = v.matcher(line);
								if (f.find()) {
									int start = line.indexOf("L") - 1;
									int end = line.lastIndexOf("/") + 1;
									if (end > 0) {
										className = line.substring(start, end);
									}
									currentClass = line.substring(start + 1);
								}
								if (a.find() | b.find() | c.find() | g.find() | h.find()) {
									findMatch = true;
									changeLocals = true;
								}
								if (d.find()) {
									methodName = line;
								}
								if (e.find()) {
									storage.put(methodName, changeLocals);
									changeLocals = false;
								}

							}


						} catch (IOException e) {
							e.printStackTrace();
						}


						// --------------------------
						// STEP 2:
						// Replace file if necessary
						// --------------------------

						if (findMatch) {

							OtherMethods om = new OtherMethods();
							File newFile = null;

							// write new template file
							className = om.writeTemplateFile(file.getParent().toString(), className, templatePath, trackName);

							// edit file
							try {
								
								// declare variables 
								
								newFile = new File(directory + "/temp.smali");
								newFile.createNewFile();
								
								FileWriter fw = new FileWriter(newFile);
								BufferedWriter bw = new BufferedWriter(fw);

								FileReader fr = new FileReader(file.toString());
								BufferedReader br = new BufferedReader(fr);
								String line = "", key = "", value = "", foString = "", contextVal = "", instanceName = "", instanceClass = "";
								boolean localChange = false, keepTrack = false, localC = false, writeAdditional = false, newInstance = true, moveResult = false;
								boolean getOutput = false, getContext = false, nextLine = false, lineAfter = false, move = false;
								int localNum = 0, writeType = 0, newReg = 0, sharedPrefNum = 0;

								Pattern p = Pattern.compile("\\.method ");
								Pattern q = Pattern.compile("\\.locals ");
								Pattern r = Pattern.compile("\\.end");
								Pattern s = Pattern.compile("SharedPreferences");
								Pattern t = Pattern.compile("openFileOutput");
								Pattern u = Pattern.compile("Landroid/database/sqlite/SQLiteDatabase;->insert");
								Pattern v = Pattern.compile(".super Landroid/database/sqlite/SQLiteOpenHelper;");
								Pattern o = Pattern.compile("# instance fields");
								Pattern w = Pattern.compile(".field private ");
								Pattern x = Pattern.compile("const");
								Pattern y = Pattern.compile("\\.local ");
								Pattern z = Pattern.compile("move-result");
								
								// parse file
								while ((line = br.readLine()) != null) {
									
									String output = "";
									
									Matcher a = p.matcher(line);
									Matcher b = q.matcher(line);
									Matcher c = r.matcher(line);
									Matcher d = s.matcher(line);
									Matcher e = t.matcher(line);
									Matcher f = u.matcher(line);
									Matcher g = v.matcher(line);
									Matcher h = w.matcher(line);
									Matcher i = x.matcher(line);
									Matcher j = y.matcher(line);
									Matcher k = z.matcher(line);
									Matcher l = o.matcher(line);
									
									
									// track values written to registers
									if (i.find() | j.find() | k.find()) {
										if (line.contains("const") & !line.contains("constructor")) {
											String t1 = om.returnVar(line);
											varStorage.put(t1, line);
										}
										else {
											String t1 = om.returnVar(line);
											if (varStorage.get(t1) != null) {
												varStorage.remove(t1);
											}
										}
									}
									
									// change .locals number
									if (b.find() & localChange) {
										localNum = om.returnReg(line);
										
										if (localNum <= 12) {
											line = om.changeReg(line);
											newReg = localNum;
										}
										else {
											System.out.println("LINE: " + line);
											localC = true;
										}
										
										
										localChange = false;
									}
									
									// check if method should be changed
									if (a.find()) {
										if (storage.get(line)) {
											localChange = true;	
										}
									}
									
									// check for end of method
									if (c.find()) {
										localChange = false;
										newInstance = true;
										varStorage.clear();
										localC = false;
									}
									
									// check move-result for get
									if (moveResult) {
										if (line.contains("move-result")) {
											Pattern p1 = Pattern.compile(" v");
											Matcher m1 = p1.matcher(line);
											if (m1.find()) {
												value = line.substring(m1.end() - 1, m1.end() + 1);
												moveResult = false;
												writeAdditional = true;
												output = om.toSSharedPref(key, value, className, newReg, sharedPrefNum, varStorage, localChange);
											}
										}
									}
									
									// check for Shared Preferences
									if (d.find()) {
										sharedPrefNum = om.checkGetPut(line);
										// get method
										if (1 <= sharedPrefNum & sharedPrefNum <= 6) {
											moveResult = true;
											key = om.getValues(line, 2);
											
											writeType = 1;
										}
										// put method
										if (7 <= sharedPrefNum & sharedPrefNum <= 12) {
											//writeAdditional = true;
											key = om.getValues(line, 2);
											value = om.getValues(line, 3);
											writeType = 1;
											output = om.toSSharedPref(key, value, className, newReg, sharedPrefNum, varStorage, localChange);
											
										}
									}
										
									// openFileOutput: find write / close
									if (getOutput) {
										if (line != null) {
											if (line.contains("Ljava/io/FileOutputStream;->write")) {
												foString = om.getVariables(line); 
											}
											else if (line.contains("Ljava/io/FileOutputStream;->close()")) {
												//System.out.format("%s: ", file);
												//System.out.println(line);
												foString = om.writeFileOut(foString, className, currentClass, localNum);
												writeAdditional = true;
												writeType = 2;
												getOutput = false;
											}
										}
									}
									
									
									// check for openFileOutput
									if (e.find()) {
										getOutput = true;
									}
									
									// SQLite: create instance 
									if (nextLine) {
										output = "# instance fields\n\n.field private final myContext:Landroid/content/Context;\n\n";
										nextLine = false;
										writeType = 3;
										writeAdditional = true;
									}
									
									// SQLite: get context
									if (lineAfter) {
										output = "iput-object p1, p0, " + currentClass + "->myContext:Landroid/content/Context;\n\n";
										lineAfter = false;
										writeType = 3;
										writeAdditional = true;
									}
									
									// SQLite: insert context calls
									if (getContext && line.contains(".source")) {
										nextLine = true;
									}
									if (getContext && line.contains("SQLiteOpenHelper;-><init>")) {
										lineAfter = true;
										getContext = false;
									}
									
									// check for SQLiteOpenHelper .super call
									if (g.find()) {
										getContext = true;
									}
									
									// get SQLite instance
									
									if (l.find()) {
										keepTrack = true;
									}
									
									if (h.find() & keepTrack) {
										if (line != null) {
											System.out.println("line: " + line);
											System.out.println("current class: " + currentClass);
											String sub = currentClass.substring(0, currentClass.length() - 1);
											instanceName = line.substring(line.lastIndexOf(" "), line.indexOf(":"));
											instanceClass = line.substring(line.indexOf(":") + 1);
											System.out.println("instance Name: " + instanceName);
											System.out.println("instance Class: " + instanceClass);
										}
									}
									
									// write SQL Database Insert
									if (move && line.contains("move-result-wide")) {
										//System.out.format("%s: ", file);
										//System.out.println(line);
										contextVal = om.writeSQLite(localNum, currentClass, instanceName, instanceClass, contextVal, className);
										writeType = 4;
										writeAdditional = true;
										move = false;
									}
									
									// check for SQLite Database Insert
									if (f.find()) {
										contextVal = om.getContextValues(line);
										move = true;
									}
									
									
									// finally, write line
									bw.write(line.concat("\n"));
									bw.flush();
									
									// check writeAdditional (add code)
									if (writeAdditional & !localC) {
										// instantiate new class
										if (newInstance && (writeType != 3)) {
											String t1 = om.checkNewInstance(localNum, className);
											//System.out.println("\nWRITING: " + t1);
											bw.write(t1.concat("\n"));
											bw.flush();
											newInstance = false;
											writeAdditional = false;
										}
										if (writeType == 1) { // Shared Preferences
											output = output.concat(om.writeShared(newReg, currentClass, className));
											//System.out.println("\nWRITING: " + output);
											bw.write(output);
											bw.flush();
											writeAdditional = false;	
										}
										else if (writeType == 2) { // File Outputs
											output = foString;
											bw.write(output);
											bw.flush();
											writeAdditional = false;
										}
										else if (writeType == 3) { // SQLite Accesses Context 
											bw.write(output);
											bw.flush();
											writeAdditional = false;
										} 
										else if (writeType == 4) { // SQLite write to output
											output = contextVal;
											//System.out.println("OUTPUT: " + output);
											bw.write(output);
											bw.flush();
											writeAdditional = false;
										}
										
									}
								}
							br.close();
							bw.close();
							
							} catch (IOException e) {
								e.printStackTrace();
							}

							// delete original, rename temporary
							File delete = new File(file.toString());
							delete.delete();

							newFile.renameTo(delete);
						}




					}


					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFileFailed(Path file, IOException exc) {
					System.err.println(exc);
					return FileVisitResult.CONTINUE;
				}

			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
