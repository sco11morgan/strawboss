package hudson.model;

import captmorgan.ExternalBuild;
import captmorgan.ExternalProject;
import hudson.model.Result;
import org.jvnet.hudson.test.HudsonTestCase;

import java.io.StringReader;

/**
 * Based off ExternalRunTest
 *
 * @author Scott Morgan
 * @author Kohsuke Kawaguchi
 */
public class ExternalBuildTest extends HudsonTestCase {
    public void test1() throws Exception {
        ExternalProject p = hudson.createProject(ExternalProject.class, "test");
        ExternalBuild b = p.newBuild();
        b.acceptRemoteSubmission(new StringReader(
            "<run><log content-encoding='UTF-8'>AAAAAAAA</log><result>0</result><duration>100</duration></run>"
        ));
        assertEquals(b.getResult(), Result.SUCCESS);
        assertEquals(b.getDuration(),100);

        b = p.newBuild();
        b.acceptRemoteSubmission(new StringReader(
            "<run><log content-encoding='UTF-8'>AAAAAAAA</log><result>1</result>"
        ));
        assertEquals(b.getResult(),Result.FAILURE);
    }
}
