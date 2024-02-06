import java.nio.channels.Channels
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

import org.kohsuke.github.GHAsset
import org.kohsuke.github.GHOrganization
import org.kohsuke.github.GHRelease
import org.kohsuke.github.GHRepository
import org.kohsuke.github.GitHub;
import org.kohsuke.github.PagedIterable

import com.neuronrobotics.bowlerstudio.scripting.PasswordManager
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine
import com.neuronrobotics.bowlerstudio.vitamins.Vitamins

import eu.mihosoft.vrl.v3d.CSG
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.exception.ZipException

@groovy.transform.Field HttpURLConnection connection
GitHub getGithub() throws IOException {
	File workspace = new File(System.getProperty("user.home") + "/bowler-workspace/");
	if (!workspace.exists()) {
		workspace.mkdir();
	}
	try {
		PasswordManager.loadLoginData(workspace);
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	//PasswordManager.login();
	GitHub gh = PasswordManager.getGithub();
	return gh;
}

void extract(String URL, File directory, String zipfileName) {
	File zipfile = new File(directory.getAbsolutePath() + "/" + zipfileName);
	if (!zipfile.exists()) {
		//String curl = "curl -sL --header \"Accept: application/octet-stream\" --header \"Authorization: token "+PasswordManager.getPassword()+"\" "+URL+" -o "+zipfile.getAbsolutePath()
		//println curl

		try {

			ProcessBuilder pb = new ProcessBuilder(
					"curl",
					"-LkJ",
					"--http2",
					"--header",
					"\"Accept: application/octet-stream\"",
					"--header",
					"\"Authorization: token "+PasswordManager.getPassword()+"\"",
					URL,
					"-o",
					zipfile.getAbsolutePath())
			String scriptContents = String.join(" ",pb.command().toArray(new String[0]))
			File script = new File(directory.getAbsolutePath() + "/run.sh" )

			BufferedWriter writer = new BufferedWriter(new FileWriter(script));
			writer.write("#!/bin/bash\n"+scriptContents);
			writer.close();
			pb = new ProcessBuilder(
				"bash",
				script.getAbsolutePath()
				)
			Process process = pb.start();

			BufferedReader reader = new BufferedReader(
					new InputStreamReader(process.getInputStream()));

			BufferedReader errorReader = new BufferedReader(
					new InputStreamReader(process.getErrorStream()));
			String line;

			while ((line = reader.readLine()) != null ||((line = errorReader.readLine()) != null)) {
				System.out.println(line);
			}
			int exitValue = process.waitFor();
			if (exitValue != 0) {
				System.out.println("Abnormal process termination "+exitValue);
			}
			reader.close();
			errorReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(zipfile.exists()) {
			println "Unzip the downloaded release "+zipfile.getAbsolutePath();
			String source = zipfile.getAbsolutePath();
			String destination =directory.getAbsolutePath();
			try {
				ZipFile zipFile = new ZipFile(source);
				zipFile.extractAll(destination);
			} catch (ZipException e) {
				e.printStackTrace();
			}
		}
	}else {
		println" Zip Extracted already"
	}
}

void loadAllFiles(ArrayList<File> files, File directory) {
	File[] filesInDir=directory.listFiles()
	boolean hasPrintBed = false;
	for(File f:filesInDir) {
		if(!f.isDirectory()){
			if(f.getName().contains("Print-Bed")) {
				hasPrintBed=true;
				files.add(f)
			}
		}else {
			loadAllFiles(files,f)
		}
	}
	if(!hasPrintBed) {
		for(File f:filesInDir) {
			if(!f.isDirectory()){
				if(f.getName().contains(".stl")&& !f.getName().contains("Part")) {
					files.add(f)
				}
			}
		}
	}
	
}

GitHub github = getGithub();

String projectDestBaseName = "BancroftKineticSystemsClass"

GHOrganization dest = github.getMyOrganizations().get(projectDestBaseName);

if (dest == null) {
	System.out.println("FAIL, you do not have access to " + projectDestBaseName);
	return;
}

PagedIterable<GHRepository> repos = dest.listRepositories();
ArrayList<File> stls = []
ArrayList<File> toLoad =[]
for (GHRepository R : repos) {

	//println R.getHtmlUrl()
	GHRelease release= R.getLatestRelease()
	if(release!=null) {
		String rGetFullName = R.getFullName()
		String tmpDirsLocation = System.getProperty("java.io.tmpdir");
		File tmp = new File(tmpDirsLocation+"/"+rGetFullName)
		tmp.mkdirs( )
		List<GHAsset>  assets= release.getAssets()
		for(GHAsset asset :assets) {
			//println tmp.getAbsolutePath()
			//println "\t"+ asset.getApiRoute()
			extract("https://api.github.com"+asset.getApiRoute(),tmp,asset.getName())
			loadAllFiles(toLoad, tmp)
		}
	}
}
ArrayList<CSG> parts = []
int count = 0
for(File f:toLoad) {
	println f.getAbsolutePath()
	String name = f.getAbsolutePath()
	CSG get = Vitamins.get(f)
				.toZMin()
	get.setName(name)
	parts.add(get)
	count++;
	if(count>11) {
		get.setPrintBedNumber(1)
	}
}

return parts

