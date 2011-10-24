package captmorgan;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.ItemGroup;
import hudson.model.Project;
import hudson.model.TopLevelItem;
import hudson.model.TopLevelItemDescriptor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 *
 *
 * @author Scott Morgan
 */
public class ExternalProject extends Project<ExternalProject, ExternalBuild> implements TopLevelItem {

    public ExternalProject(ItemGroup parent, String name) {
        super(parent, name);
    }

    @Override
    protected Class<ExternalBuild> getBuildClass() {
        return ExternalBuild.class;
    }

//    @Override
//    protected void reload() {
//        this.runs.load(this,new RunMap.Constructor<ExternalBuild>() {
//            public ExternalBuild create(File dir) throws IOException {
//                return new ExternalBuild(ExternalProject.this,dir);
//            }
//        });
//    }

    /**
     * Used to check if this is an external job and ready to accept a build result.
     */
    public void doAcceptBuildResult(StaplerResponse rsp) throws IOException, ServletException {
        rsp.setStatus(HttpServletResponse.SC_OK);
    }

    /**
     * Used to post the build result from a remote machine.
     */
    public void doPostBuildResult( StaplerRequest req, StaplerResponse rsp ) throws IOException, ServletException {
        checkPermission(AbstractProject.BUILD);
        ExternalBuild run = newBuild();
        run.acceptRemoteSubmission(req.getReader());
        rsp.setStatus(HttpServletResponse.SC_OK);
    }


    @Extension(ordinal=1)
    public static final TopLevelItemDescriptor DESCRIPTOR = new DescriptorImpl();

    public TopLevelItemDescriptor getDescriptor() {
        return DESCRIPTOR;
    }

     public static final class DescriptorImpl extends TopLevelItemDescriptor {
        public String getDisplayName() {
            return "Strawboss: External Job Monitor";
        }

         public ExternalProject newInstance(ItemGroup parent, String name) {
            return new ExternalProject(parent,name);
        }
    }
}
