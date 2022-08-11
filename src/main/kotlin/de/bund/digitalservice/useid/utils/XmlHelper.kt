package de.bund.digitalservice.useid.utils

import java.io.StringWriter
import javax.xml.bind.JAXBContext
import javax.xml.bind.JAXBException
import javax.xml.bind.Marshaller

object XmlHelper {
    /**
     * This method marshals any element that is annotated with [javax.xml.bind.annotation.XmlRootElement].
     *
     * @param inputObject the annotated XML object
     * @return the string representation of the XML object
     */
    fun marshalObject(inputObject: Any): String {
        return try {
            val jaxbContext = JAXBContext.newInstance(inputObject.javaClass)
            val marshaller = jaxbContext.createMarshaller()
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true)
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8")
            val stringWriter = StringWriter()
            stringWriter.use {
                marshaller.marshal(inputObject, stringWriter)
            }
            stringWriter.toString()
        } catch (e: JAXBException) {
            throw IllegalStateException("error while marshalling class " + inputObject.javaClass.name, e)
        }
    }
}
