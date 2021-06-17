ALTER TABLE IF EXISTS ONLY post DROP CONSTRAINT IF EXISTS pk_post_id CASCADE;

DROP TABLE IF EXISTS post;
CREATE TABLE post (
                      id SERIAL NOT NULL,
                      person_id INTEGER NOT NULL,
                      message TEXT NOT NULL,
                      has_image BOOLEAN DEFAULT NUll,
                      has_video BOOLEAN DEFAULT NUll,
                      post_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
                      adom_count INTEGER DEFAULT 0,
                      comment_count INTEGER DEFAULT 0,
                      share_count INTEGER DEFAULT 0,
                      image_count INTEGER DEFAULT 0
);

ALTER TABLE ONLY post
    ADD CONSTRAINT pk_post_id PRIMARY KEY (id);