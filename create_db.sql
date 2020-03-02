/*
 *    Copyright (c) 2020, Needham Software LLC
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

-- Database creation, obviously customize for your needs. Mostly putting this here so
-- I don't have to re-investigate the char set issues with mysql/mariadb repeatedly
-- (https://mathiasbynens.be/notes/mysql-utf8mb4) typically I just log in as root and paste
CREATE DATABASE app CHARACTER SET = utf8mb4 COLLATE utf8mb4_unicode_ci