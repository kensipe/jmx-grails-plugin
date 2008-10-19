import org.codehaus.groovy.grails.commons.GrailsClassUtils as GCU

Ant.property(environment: 'env')
grailsHome = Ant.antProject.properties.'env.GRAILS_HOME'
srcDir = 'src/java'

target ('default': 'Creates a Spring XML file with Hibernate Support... create destroys what is there.') {

	// should be in the grails-app/conf/spring/resources.xml 
	
	def moduleName = "resources.xml"
    def moduleFile = "grails-app/conf/spring/${moduleName}"
    
    def templatePath = "${jmxPluginDir}/src/templates"
    def templateFile = "${templatePath}/resources.xml"

    // Check whether the target module exists already.
    if (new File(moduleFile).exists()) {
        // It does, so find out whether the user wants to overwrite
        // the existing copy.
        Ant.input(
            addProperty:"${moduleName}.overwrite",
            message:"JmxModule: ${moduleName} already exists. Overwrite? [y/n]")

        if (Ant.antProject.properties."${moduleName}.overwrite" == "n") {
            // User doesn't want to overwrite, so stop the script.
            return
        }
    }

    // Copy the template module file over, replacing any tokens in the
    // process.
    Ant.copy(file: templateFile, tofile: moduleFile, overwrite: true)

   // apparently don't know enough yet :)  Patience my precious... Patience 
   //  event("CreatedFile", [ moduleFile ])
}
