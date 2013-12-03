importPackage(java.lang);
importPackage(java.io);
importPackage(java.net);
importPackage(java.util);
importPackage(java.text);
importPackage(javax.servlet);
importPackage(javax.servlet.http);

function load(location) {
    var url;
    if (location.indexOf(':/') > 0) url= new URL(location);
    else url = url = new File(location).toURI().toURL();
    var br = new BufferedReader(new InputStreamReader(url.openStream()));
    var script = new StringBuilder();
    while ((line = br.readLine()) !== null) {
        script.append(line);
        script.append('\n');
    }
    eval(script.toString);
}

function addServlet(path, handler, init, destroy) {
	var delegate = {
		init: function(filterConfig) {
			if (init) init(filterConfig);
		},
		destroy: function() {
			if (destroy) destroy();
		},
		doFilter: function(req, res) {
			if (handler) handler(req, res);
		}
	};
	var filterChain = new FilterChain(delegate);
	servlet.add(path, filterChain);
}

function removeServlet(name) {
	servlet.remove(name);
}