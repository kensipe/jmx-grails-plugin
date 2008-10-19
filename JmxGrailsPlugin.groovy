import org.springframework.jmx.support.MBeanServerFactoryBean
import org.springframework.jmx.export.MBeanExporter
import org.hibernate.jmx.StatisticsService

import org.codehaus.groovy.grails.commons.GrailsClassUtils as GCU

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

        // there is an mbean already defined with hibernate
        hibernateStats(StatisticsService) {
		    statisticsEnabled = true
		    sessionFactory = ref("sessionFactory")
	    }

        // configure the expo
        exporter(MBeanExporter) {
             server = ref(mbeanServer)
             beans = [:]
        }

    }
   
    def doWithApplicationContext = { ctx ->

        // TODO add a bean to spring on the fly
        // TODO add a logging wrapper
        // TODO allow for static expose = ['jmx:ObjectName']

        def configDomain = "GrailsConfig"
        def appDomain = "GrailsApp"
        println "****************** do with app ctx ****************"

        MBeanExporter exporter = ctx.getBean("exporter")
        exportConfigBeans(exporter, ctx, configDomain)

 
        application.serviceClasses?.each { service ->
          def serviceClass = service.getClazz()
          def serviceName = service.shortName

          def exposeList = GCU.getStaticPropertyValue(serviceClass, 'expose')
          if (exposeList != null && exposeList.contains('jmx')) {
               // perhaps these should be limited to singletons
              exporter.beans."${appDomain}:service=${serviceName}" = service
          }
        }

        exportMBeans(exporter)

    }

    private def exportMBeans(MBeanExporter exporter) {
        exporter.unregisterBeans()
        exporter.registerBeans()
    }

    private def exportConfigBeans(MBeanExporter exporter, ctx, configDomain) {
        exporter.beans."${configDomain}:service=statistics,type=hibernate" = ctx.hibernateStats
        exporter.beans."${configDomain}:service=datasource" = ctx.dataSource
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
