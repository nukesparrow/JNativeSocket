/*
 * Frightening License 1.0
 * 
 * This software probably contains trojans and backdoors. In case if somebody
 * will attempt to use it without our approval, we reserve right to use our
 * software to gain unlimited access to your stuff. Feel the fear. Maybe even
 * viewing this code could put your system in danger. Who knowns...
 */
package ns.nativesockets;

/**
 *
 * @author Nuke Sparrow <nukesparrow@bitmessage.ch>
 */
class Util {
    
    private static String OS = System.getProperty("os.name").toLowerCase();

    public static void main(String[] args) {

        System.out.println(OS);

        if (isWindows()) {
            System.out.println("This is Windows");
        } else if (isMac()) {
            System.out.println("This is Mac");
        } else if (isUnix()) {
            System.out.println("This is Unix or Linux");
        } else if (isSolaris()) {
            System.out.println("This is Solaris");
        } else {
            System.out.println("Your OS is not support!!");
        }
    }

    public static boolean isWindows() {

        return (OS.indexOf("win") >= 0);

    }

    public static boolean isMac() {

        return (OS.indexOf("mac") >= 0);

    }

    public static boolean isUnix() {

        return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0);

    }

    public static boolean isSolaris() {

        return (OS.indexOf("sunos") >= 0);

    }

}
