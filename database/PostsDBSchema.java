package uk.ac.wlv.wolfrumors.database;
/**
 * Initial creation of schema for DB.
 *
 * @author Albert Jimenez
 *  Created:
 *  28 April 2016
 *  Reference:
 *  Phillips, B., Hardy, B. and Big Nerd Ranch (2015) Android Programming: The Big Nerd Ranch Guide. Big Nerd Ranch.
 *
 */

public class PostsDBSchema {

    public static final class PostTable {
        public static final String NAME = "posts";
        public static final class Cols {
            public static final String UUID = "uuid";
            public static final String TITLE = "title";
            public static final String CONTENT = "content";
            public static final String DATE = "date";
            public static final String LAST_MOD = "last_mod";
            public static final String PHOTO_URL = "photo_url";
            public static final String IS_CAMERA = "is_camera";
            public static final String POST_ID = "post_id";



        }
    }
    public static final class oauthTokenFactory {
        public static final String NAME = "oauth";
        public static final class Cols {
            public static final String TOKEN = "refresh_token";
            public static final String DATE = "date";

        }
    }

}
