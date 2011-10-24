package captmorgan;

import hudson.model.Build;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.util.DecodingStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.util.Calendar;

import static javax.xml.stream.XMLStreamConstants.*;

/**
 *
 *
 * @author Scott Morgan
 */
public class ExternalBuild extends Build<ExternalProject, ExternalBuild> {
    public ExternalBuild(ExternalProject job, Calendar timestamp) {
        super(job, timestamp);
    }

    public ExternalBuild(ExternalProject project, File buildDir) throws IOException {
        super(project, buildDir);
    }

    public ExternalBuild(ExternalProject project) throws IOException {
        super(project);
    }

    @Override
    public void run() {
        System.out.println("===== straw running");
        super.run();    //To change body of overridden methods use File | Settings | File Templates.
    }

//    /**
//     * Instead of performing a build, run the specified command,
//     * record the log and its exit code, then call it a build.
//     */
//    public void qrun(final String[] cmd) {
//        run(new SCMTrigger.Runner() {
//            public Result run(BuildListener listener) throws Exception {
//                Proc proc = new Proc.LocalProc(cmd,getEnvironment(listener),System.in,new DualOutputStream(System.out,listener.getLogger()));
//                return proc.join()==0?Result.SUCCESS:Result.FAILURE;
//            }
//
//            public void post(BuildListener listener) {
//                // do nothing
//            }
//
//            public void cleanUp(BuildListener listener) {
//                // do nothing
//            }
//        });
//    }

    /**
     * Instead of performing a build, accept the log and the return code
     * from a remote machine.
     *
     * <p>
     * The format of the XML is:
     *
     * <pre><xmp>
     * <run>
     *  <log>...console output...</log>
     *  <result>exit code</result>
     * </run>
     * </xmp></pre>
     */
    @SuppressWarnings({"Since15"})
    public void acceptRemoteSubmission(final Reader in) throws IOException {
        final long[] duration = new long[1];
        run(new Runner() {
            private String elementText(XMLStreamReader r) throws XMLStreamException {
                StringBuilder buf = new StringBuilder();
                while(true) {
                    int type = r.next();
                    if(type== CHARACTERS || type== CDATA)
                        buf.append(r.getTextCharacters(), r.getTextStart(), r.getTextLength());
                    else
                        return buf.toString();
                }
            }

            public Result run(BuildListener listener) throws Exception {
                PrintStream logger = new PrintStream(new DecodingStream(listener.getLogger()));

                XMLInputFactory xif = XMLInputFactory.newInstance();
                XMLStreamReader p = xif.createXMLStreamReader(in);

                p.nextTag();    // get to the <run>
                p.nextTag();    // get to the <log>

                charset=p.getAttributeValue(null,"content-encoding");
                while(p.next()!= END_ELEMENT) {
                    int type = p.getEventType();
                    if(type== CHARACTERS || type== CDATA)
                        logger.print(p.getText());
                }
                p.nextTag(); // get to <result>



                Result r = Integer.parseInt(elementText(p))==0?Result.SUCCESS:Result.FAILURE;

                do {
                    p.nextTag();
                    if(p.getEventType()== START_ELEMENT){
                        if(p.getLocalName().equals("duration")) {
                            duration[0] = Long.parseLong(elementText(p));
                        }
                        else if(p.getLocalName().equals("displayName")) {
                            setDisplayName(p.getElementText());
                        }
                        else if(p.getLocalName().equals("description")) {
                            setDescription(p.getElementText());
                        }
                    }
                } while(!(p.getEventType() == END_ELEMENT && p.getLocalName().equals("run")));

                return r;
            }

            public void post(BuildListener listener) {
                // do nothing
            }

            public void cleanUp(BuildListener listener) {
                // do nothing
            }
        });

        if(duration[0]!=0) {
            super.duration = duration[0];
            // save the updated duration
            save();
        }
    }

}
