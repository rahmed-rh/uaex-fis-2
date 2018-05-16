openshift.withCluster() {
 openshift.withCredentials('cluster-admin-credential-id') {
  def PROJECT_NAME = "poc"
  def cm


  openshift.withProject(PROJECT_NAME) {

   echo "Hello from project ${openshift.project()} in cluster ${openshift.cluster()}"

   // Mark the code checkout 'stage'....
   stage('Read CM') {

    def cmSelector = openshift.selector("configmap", "pipeline-app-config")
    def cmExists = cmSelector.exists()

    if (cmExists) {
     cm = cmSelector.object()
    } else {
     error("Build failed Application ConfigMap is not found, try to run infra pipeline first.")
    }
    echo "The CM is ${cm}"
    echo "The fis-1-app-git-url is ${cm.data['fis-1-app-git-url']}"
    echo "The fis-1-app-name is ${cm.data['fis-1-app-name']}"
    echo "The fis-2-app-git-url is ${cm.data['fis-2-app-git-url']}"
    echo "The fis-2-app-name is ${cm.data['fis-2-app-name']}"

   }

   node('maven') {
    // Mark the code checkout 'stage'....
    stage('Checkout') {

      // Get some code from a GitHub repository
      git branch: "master", url: cm.data['fis-2-app-git-url']
     }
     // Mark the code build 'stage'....
    stage('Maven Build') {
     // Run the maven build
     sh "mvn clean compile -s settings.xml"
    }
    stage('Deploy to DEV') {
    	//echo "Deleting OLD JDG Env if exist"
	    //openshift.selector( 'all', [ application: cm.data['fis-1-app-name'] ] ).delete()
	    //openshift.selector( 'pvc', [ application: cm.data['fis-1-app-name'] ] ).delete()
     	// Run the fabric8
     	sh "mvn fabric8:deploy -s settings.xml"
    }

   }

  }

 }
}