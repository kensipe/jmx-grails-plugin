import org.springframework.jmx.support.MBeanServerFactoryBean
import org.springframework.jmx.export.MBeanExporter
import org.hibernate.jmx.StatisticsService

class JmxGrailsPlugin {
    def version = 0.2
    def dependsOn = [:]

   
    def author = "Ken Sipe"
    def authorEmail = "kensipe@gmail.com"
    def title = "The JMX Grails Plugin"
    def description = '''\
Adds JMX supporrt to any Grails application.
TODO: jmx services (similar to Gwt remoting of service)
TODO: jmx controllers with: static exposed = ['jmx:ObjectName']
TODO: jmx jetty

'''

    // URL to the plugin's documentation
    def documentation = "http://grails.org/JmxGrailsPlugin+Plugin"

    def doWithSpring = {

        // adding the mbean server configuration and export with mbeanserver ref... no exports
        mbeanServer(MBeanServerFactoryBean) {
            locateExistingServerIfPossible = true
        }

        hibernateStats(StatisticsService) {
		    statisticsEnabled = true
		    sessionFactory = ref("sessionFactory")
	    }

        exporter(MBeanExporter) {
             server = ref(mbeanServer)
             beans = [:]
        }

    }
   
    def doWithApplicationContext = { ctx ->

        // TODO add a bean to spring on the fly
        // TODO discover services on the fly and register them with a convention
        // TODO add a logging wrapper


        println "****************** do with app ctx ****************"

        MBeanExporter exporter = ctx.getBean("exporter")
        def stats = ctx.getBean("hibernateStats")
        def ds =ctx.getBean("dataSource")

        exporter.beans."GrailsConfig:service=statistics,type=hibernate" = stats
        exporter.beans."GrailsConfig:service=datasource" = ctx.dataSource

        exporter.unregisterBeans()
        exporter.registerBeans()

    }

    def doWithWebDescriptor = { xml ->

    }
	                                      
    def doWithDynamicMethods = { ctx ->

    }
	
    def onChange = { event ->

    }

    def onConfigChange = { event ->

    }
}
