package org.springdoclet

@SuppressWarnings("GroovyVariableNotAssigned")
class RequestMappingCollector {
  private static String MAPPING_TYPE = 'org.springframework.web.bind.annotation.RequestMapping'
  private static String METHOD_TYPE = 'org.springframework.web.bind.annotation.RequestMethod.'
  private mappings = []

  void processClass(classDoc, annotations) {
    def annotation = getMappingAnnotation(annotations)
    if (annotation) {
      def (rootPath, defaultHttpMethods) = getMappingElements(annotation)
      processMethods classDoc, rootPath ?: "", defaultHttpMethods ?: ['GET']
    } else {
      processMethods classDoc, "", ['GET']
    }
  }

  private void processMethods(def classDoc, def rootPath, def defaultHttpMethods) {
    def methods = classDoc.methods(true)
    for (method in methods) {
      for (annotation in method.annotations()) {
        def annotationType = Annotations.getTypeName(annotation)
        if (annotationType?.startsWith(MAPPING_TYPE)) {
          processMethod classDoc, rootPath, defaultHttpMethods, annotation
        }
      }
    }
  }

  private def processMethod(classDoc, rootPath, defaultHttpMethods, annotation) {
    def (path, httpMethods) = getMappingElements(annotation)
    for (httpMethod in (httpMethods ?: defaultHttpMethods)) {
      addMapping classDoc, "$rootPath$path", httpMethod
    }
  }

  def getMappingAnnotation(annotations) {
    for (annotation in annotations) {
      def annotationType = Annotations.getTypeName(annotation)
      if (annotationType?.startsWith(MAPPING_TYPE)) {
        return annotation
      }
    }
    return null
  }

  private def getMappingElements(annotation) {
    def elements = annotation.elementValues()
    def path = getElement(elements, "value") ?: ""
    def httpMethods = getElement(elements, "method")?.value()
    return [path, httpMethods]
  }

  private def getElement(elements, key) {
    for (element in elements) {
      if (element.element().name() == key) {
        return element.value()
      }
    }
    return null
  }

  private void addMapping(classDoc, path, httpMethod) {
    def httpMethodName = httpMethod.toString() - METHOD_TYPE
    mappings << [path: path, httpMethodName: httpMethodName, className: classDoc.qualifiedTypeName()]
  }

  String toString() {
    def str = new StringBuffer('RequestMappings:\n')
    def sortedMappings = mappings.sort { it.path }
    for (mapping in sortedMappings) {
      str << "${mapping.httpMethodName} ${mapping.path}: ${mapping.className}\n"
    }
    return str
  }
}