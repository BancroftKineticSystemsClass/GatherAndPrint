import org.kohsuke.github.GHOrganization
import org.kohsuke.github.GitHub;

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

