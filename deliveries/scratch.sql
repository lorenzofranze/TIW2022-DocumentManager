--ho dato il nome db_docmanager allo schema solo per avere lo stesso nome nel web.xml

CREATE TABLE  user (
    username VARCHAR(16) NOT NULL PRIMARY KEY,
    email VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(16) NOT NULL,
    password VARCHAR(40) NOT NULL
);

CREATE TABLE folder (
    username VARCHAR(16) NOT NULL,
    foldername VARCHAR(40) NOT NULL,
    date DATE NOT NULL,
    PRIMARY KEY (username, foldername),
    FOREIGN KEY (username)
        REFERENCES user (username)
        ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE subfolder (
    username VARCHAR(16) NOT NULL,
    foldername VARCHAR(40) NOT NULL,
    subfoldername VARCHAR(40) NOT NULL,
    date DATE NOT NULL,
    PRIMARY KEY (username, foldername, subfoldername),
    FOREIGN KEY (username, folderName)
        REFERENCES folder (username, folderName)
        ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE document (
    username VARCHAR(16) NOT NULL,
    foldername VARCHAR(40) NOT NULL,
    subfoldername VARCHAR(40) NOT NULL,
    documentname VARCHAR(40) NOT NULL,
    type VARCHAR(16) NOT NULL,
    summary VARCHAR(255) NOT NULL,
    date DATE NOT NULL,
    body blob not null,
    PRIMARY KEY (username, foldername, subfoldername, documentname, type),
    FOREIGN KEY (username, folderName, subfoldername)
        REFERENCES subfolder (username, foldername, subfoldername)
        ON DELETE CASCADE ON UPDATE CASCADE
);
