package xyz.lucasallegri.util;


import java.util.List;

import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

public class TwitterUtil {
	
	public static List<Status> getStatuses(String user, int count) throws TwitterException {
		
		Twitter unauthenticatedTwitter = new TwitterFactory().getInstance();
		Paging paging = new Paging(1, count);
		List<Status> statuses = unauthenticatedTwitter.getUserTimeline(user, paging);
		return statuses;
	}

}
