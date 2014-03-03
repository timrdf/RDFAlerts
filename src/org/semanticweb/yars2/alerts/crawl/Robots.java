package org.semanticweb.yars2.alerts.crawl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

import org.osjava.norbert.NoRobotClient;
import org.osjava.norbert.NoRobotException;


public class Robots {
	Logger _log = Logger.getLogger(this.getClass().getName());

	NoRobotClient _nrc = null;

	public Robots(String host) {
    	_nrc = new NoRobotClient(Fetch.USERAGENT);
    	try {
    		_nrc.parse( new URL( "http://" + host + "/" ) );
		} catch (MalformedURLException e) {
			_log.info(e.getMessage());
			_nrc = null;
		} catch (NoRobotException e) {
			_log.info(e.getMessage());
			_nrc = null;
		}
	}
	
    public boolean accessOK(URL uri) {
    	if (_nrc == null) {
    		return true;
    	}

    	return _nrc.isUrlAllowed(uri);
    }
}