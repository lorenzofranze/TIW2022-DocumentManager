CREATE TABLE  "user" (
    "username" VARCHAR(16) NOT NULL PRIMARY KEY,
    "password" VARCHAR(40),
);

CREATE TABLE "folder" (
    "username" VARCHAR(16),
    "foldername" VARCHAR(40),
    "date" DATE,
    PRIMARY KEY (username, foldername),
    FOREIGN KEY (username)
        REFERENCES user (username)
        ON DELETE CASCADE ON UPDATE CASCADE,
);

CREATE TABLE "subfolder" (
    "username" VARCHAR(16),
    "foldername" VARCHAR(40),
    "subfoldername" VARCHAR(40),
    "date" DATE,
    PRIMARY KEY (username, foldername, subfoldername)
    FOREIGN KEY (username)
        REFERENCES username (folder)
        ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (foldername)
        REFERENCES foldername (folder)
        ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE "document" (
    "username" VARCHAR(16),
    "foldername" VARCHAR(40),
    "subfoldername" VARCHAR(40),
    "documentname" VARCHAR(40),
    "type" VARCHAR(16),
    "summary" VARCHAR(255),
    "date" DATE
    PRIMARY KEY (username, foldername, subfoldername, documentname),
    FOREIGN KEY (username)
        REFERENCES username (subfolder)
        ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (foldername)
        REFERENCES foldername (subfolder)
        ON DELETE CASCADE ON UPDATE CASCADE
    FOREIGN KEY (subfoldername)
        REFERENCES subfoldername (subfolder)
        ON DELETE CASCADE ON UPDATE CASCADE
);