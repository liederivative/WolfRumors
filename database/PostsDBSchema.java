package uk.ac.wlv.wolfrumors.database;

/**
 * Created by user on 4/28/2016.
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



        }
    }

}
