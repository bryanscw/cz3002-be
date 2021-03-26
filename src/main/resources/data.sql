-- Username: admin1@test.com, Password: admin01
INSERT INTO qwerty.app_user (email, pass, role, name, dob, gender) VALUES ('admin1@test.com', '$2y$12$xT.zKSUW69yMuKT5oV.BL.dumTRndnEksLbkRQ2Gn.6/j2YvOShM.', 'ROLE_ADMIN', 'admin1', '2005-05-02', '1');

-- Username: admin2@test.com, Password: admin02
INSERT INTO qwerty.app_user (email, pass, role, name, dob, gender) VALUES ('admin2@test.com', '$2y$12$fQ/wnljrr9c/.N4EEr5eeOf0yFEn1OxawCZHI0vxhnDZ.2keDwEpy', 'ROLE_ADMIN', 'admin2', '2009-06-27', '0');

-- Username: patient1@test.com, Password: patient01
INSERT INTO qwerty.app_user (email, pass, role, name, dob, gender) VALUES ('patient1@test.com', '$2y$12$8YL9bJU/dqWCx9oTpVZRMewNPL6tj3.dw7SaPNFrSkSi1eC9gi4R6', 'ROLE_PATIENT', 'patient1', '1967-01-02', '1');

-- Username: patient2@test.com, Password: patient02
INSERT INTO qwerty.app_user (email, pass, role, name, dob, gender) VALUES ('patient2@test.com', '$2y$12$hNkcTjlZyHtyCqBazIQyGOKsMtdTrHhNm9Zy9OJdnbki17opZdaIC', 'ROLE_PATIENT', 'patient2', '1956-09-11', '0');

-- Username: doctor1@test.com, Password: doctor01
INSERT INTO qwerty.app_user (email, pass, role, name, dob, gender) VALUES ('doctor1@test.com', '$2y$12$bz0DAPR/tl9g1RCM7Xcd1uRk22be4UUtcisBBmCuRSkbTBteP4/pW', 'ROLE_DOCTOR', 'doctor1', '1997-01-02', '1');

-- Username: doctor2@test.com, Password: doctor02
INSERT INTO qwerty.app_user (email, pass, role, name, dob, gender) VALUES ('doctor2@test.com', '$2y$12$oK0EgHJ5Ya.pIoAwy77kaeTFfec6OW0N/NyVPPQY40AfLvwNF36Oi', 'ROLE_DOCTOR', 'doctor2', '1984-09-11', '0');

INSERT INTO qwerty.result (id, created_by, created_date, last_modified_by, last_modified_date, accuracy, time, node_num, user_id) VALUES (1, 'doctor1@test.com', '2021-02-16 02:49:40.502000000', 'patient1@test.com', '2021-02-16 02:49:40.502000000', 78.9, 54.2, 15, 'patient1@test.com');
INSERT INTO qwerty.result (id, created_by, created_date, last_modified_by, last_modified_date, accuracy, time, node_num, user_id) VALUES (2, 'doctor1@test.com', '2021-02-16 02:49:40.502000000', 'patient1@test.com', '2021-02-16 02:49:40.502000000', 85.1, 63.8, 16, 'patient1@test.com');
INSERT INTO qwerty.result (id, created_by, created_date, last_modified_by, last_modified_date, accuracy, time, node_num, user_id) VALUES (3, 'doctor1@test.com', '2021-02-16 02:49:40.502000000', 'patient1@test.com', '2021-02-16 02:49:40.502000000', 95.8, 78.1, 17, 'patient1@test.com');
INSERT INTO qwerty.result (id, created_by, created_date, last_modified_by, last_modified_date, accuracy, time, node_num, user_id) VALUES (4, 'doctor1@test.com', '2021-02-16 02:49:40.502000000', 'patient1@test.com', '2021-02-16 02:49:40.502000000', 99.2, 88.1, 18, 'patient1@test.com');


INSERT INTO qwerty.diagnosis (id, created_by, created_date, last_modified_by, last_modified_date, description, label, user_id, result_id) VALUES (1, 'doctor2@test.com', '2021-02-16 02:54:29.079000000', 'doctor2@test.com', '2021-02-16 02:54:29.079000000', 'Sample Description', 'High', 'doctor2@test.com', 1);
