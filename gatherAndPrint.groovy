import org.kohsuke.github.GHAsset
import org.kohsuke.github.GHOrganization
import org.kohsuke.github.GHRelease
import org.kohsuke.github.GHRepository
import org.kohsuke.github.GitHub;
import org.kohsuke.github.PagedIterable

import com.neuronrobotics.bowlerstudio.scripting.PasswordManager

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

GitHub github = getGithub();

String projectDestBaseName = "BancroftKineticSystemsClass"

GHOrganization dest = github.getMyOrganizations().get(projectDestBaseName);

if (dest == null) {
	System.out.println("FAIL, you do not have access to " + projectDestBaseName);
	return;
}

PagedIterable<GHRepository> repos = dest.listRepositories();
for (GHRepository R : repos) {
	String rGetFullName = R.getFullName()
	String tmpDirsLocation = System.getProperty("java.io.tmpdir");
	File tmp = new File(tmpDirsLocation+"/"+rGetFullName)
	tmp.mkdirs( )
	//println R.getHtmlUrl()
	GHRelease release= R.getLatestRelease()
	if(release!=null) {
		println rGetFullName
		List<GHAsset>  assets= release.getAssets()
		for(GHAsset asset :assets) {
			println "\t"+ asset.browserDownloadUrl
		}
	}
}



