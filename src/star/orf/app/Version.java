/* Created by JReleaseInfo AntTask from Open Source Competence Group */
/* Creation date Fri Oct 07 11:18:56 EDT 2011 */
package star.orf.app;

import java.util.Date;

/**
 * This class provides information gathered from the build environment.
 * 
 * @author JReleaseInfo AntTask
 */
public class Version {


   /** buildDate (set during build process to 1318000736703L). */
   private static Date buildDate = new Date(1318000736703L);

   /**
    * Get buildDate (set during build process to Fri Oct 07 11:18:56 EDT 2011).
    * @return Date buildDate
    */
   public static final Date getBuildDate() { return buildDate; }


   /** project (set during build process to "StarORF"). */
   private static String project = new String("StarORF");

   /**
    * Get project (set during build process to "StarORF").
    * @return String project
    */
   public static final String getProject() { return project; }

}
