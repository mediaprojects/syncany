/*
 * Syncany, www.syncany.org
 * Copyright (C) 2011-2013 Philipp C. Heckel <philipp.heckel@gmail.com> 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.syncany.tests.util;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.syncany.chunk.Transformer;
import org.syncany.database.Branch;
import org.syncany.database.Database;
import org.syncany.database.DatabaseDAO;
import org.syncany.database.DatabaseVersion;
import org.syncany.database.DatabaseVersionHeader;
import org.syncany.database.FileVersion;
import org.syncany.database.XmlDatabaseDAO;
import org.syncany.database.VectorClock;
import org.syncany.database.FileVersion.FileStatus;
import org.syncany.database.FileVersion.FileType;

public class TestDatabaseUtil {
	private static Pattern databaseVersionHeaderPattern = Pattern.compile("([^/]+)/\\(([^)]+)\\)/T=?(\\d+)");
	private static Pattern vectorClockElementPattern = Pattern.compile("([^\\d]+)(\\d+)");
	
	public static DatabaseVersionHeader createFromString(String databaseVersionHeaderString) throws Exception {
		Matcher databaseVersionHeaderMatcher = databaseVersionHeaderPattern.matcher(databaseVersionHeaderString);
		
		if (!databaseVersionHeaderMatcher.matches()) {
			throw new Exception("Invalid database version header string: "+databaseVersionHeaderString);
		}
		
		String client = databaseVersionHeaderMatcher.group(1);
		String vectorClockString = databaseVersionHeaderMatcher.group(2);
		long databaseVersionHeaderTime = Long.parseLong(databaseVersionHeaderMatcher.group(3));
		
		String[] vectorClockElements = vectorClockString.split(",");		
		VectorClock vectorClock = new VectorClock();
		
		for (String vectorClockElement : vectorClockElements) {
			if ("".equals(vectorClockElement.trim())) {
				continue;
			}
			
			Matcher vectorClockElementMatcher = vectorClockElementPattern.matcher(vectorClockElement);
			
			if (!vectorClockElementMatcher.matches()) {
				throw new Exception("Invalid vector clock element string: "+vectorClockElement);
			}
			
			String vectorClockMachineName = vectorClockElementMatcher.group(1);
			long vectorClockTime = Long.parseLong(vectorClockElementMatcher.group(2));
			
			vectorClock.setClock(vectorClockMachineName, vectorClockTime);
		}
		
		DatabaseVersionHeader newDatabaseVersionHeader = new DatabaseVersionHeader();
		
		newDatabaseVersionHeader.setDate(new Date(databaseVersionHeaderTime));
		newDatabaseVersionHeader.setVectorClock(vectorClock);
		newDatabaseVersionHeader.setClient(client);	
		
		return newDatabaseVersionHeader;
	}
	
	public static TreeMap<String, DatabaseVersionHeader> createMapWithMachineKey(String[] keysAndDatabaseVersionHeaderStrings) throws Exception {
		TreeMap<String, DatabaseVersionHeader> databaseVersionHeaderMap = new TreeMap<String, DatabaseVersionHeader>();
		
		for (int i=0; i<keysAndDatabaseVersionHeaderStrings.length; i+=2) {			
			String machineName = keysAndDatabaseVersionHeaderStrings[i];
			DatabaseVersionHeader databaseVersionHeader = createFromString(keysAndDatabaseVersionHeaderStrings[i+1]);

			databaseVersionHeaderMap.put(machineName, databaseVersionHeader);
		}

		return databaseVersionHeaderMap;
	}

	public static Branch createBranch(String[] databaseVersionHeaderStrings) throws Exception {
		Branch branch = new Branch();
		
		for (String databaseVersionHeaderString : databaseVersionHeaderStrings) {
			DatabaseVersionHeader databaseVersionHeader = createFromString(databaseVersionHeaderString);
			branch.add(databaseVersionHeader);
		}

		return branch;
	}
	
	public static Database readDatabaseFileFromDisk(File databaseFile, Transformer transformer) throws IOException {
		Database db = new Database();
		
		DatabaseDAO dao = new XmlDatabaseDAO(transformer);
		dao.load(db, databaseFile);
		
		return db;
	}
	
	public static void writeDatabaseFileToDisk(Database db, File writtenDatabaseFile, Transformer transformer) throws IOException {
		DatabaseDAO dao = new XmlDatabaseDAO(transformer);
		dao.save(db, writtenDatabaseFile);
	}
	
	public static FileVersion createFileVersion(String path) {
		FileVersion fileVersion = new FileVersion();
		
		fileVersion.setChecksum(TestFileUtil.createRandomArray(20));
		fileVersion.setCreatedBy("A");		
		fileVersion.setLastModified(new Date());
		fileVersion.setPath(path);
		fileVersion.setStatus(FileStatus.NEW);
		fileVersion.setType(FileType.FILE);
		fileVersion.setUpdated(new Date());
		fileVersion.setVersion(1L);
		
		return fileVersion;
	}
	
	public static FileVersion createFileVersion(String path, FileVersion basedOnFileVersion) {
		FileVersion fileVersion = basedOnFileVersion.clone();
		
		fileVersion.setPath(path);
		fileVersion.setStatus(FileStatus.CHANGED);
		fileVersion.setVersion(basedOnFileVersion.getVersion()+1);
		
		return fileVersion;
	}

	// TODO [medium] Add functionality tests for the rest of the cache
	// TODO [high] Add performance tests for the cache and optimize Database.addDatabaseVersion()-cache handling
	
	public static DatabaseVersion createDatabaseVersion() {
		return createDatabaseVersion(null, new Date());
	}
	
	public static DatabaseVersion createDatabaseVersion(DatabaseVersion basedOnDatabaseVersion) {
		return createDatabaseVersion(basedOnDatabaseVersion, new Date());
	}
	
	public static DatabaseVersion createDatabaseVersion(Date date) {
		return createDatabaseVersion(null, date);
	}
	
	public static DatabaseVersion createDatabaseVersion(DatabaseVersion basedOnDatabaseVersion, Date date) {
		VectorClock vectorClock = (basedOnDatabaseVersion != null) ? basedOnDatabaseVersion.getVectorClock().clone() : new VectorClock();
		vectorClock.incrementClock("someclient");
		
		DatabaseVersion databaseVersion = new DatabaseVersion();
		
		databaseVersion.setClient("someclient");
		databaseVersion.setTimestamp(date);
		databaseVersion.setVectorClock(vectorClock);
		
		return databaseVersion;
	}

	
}
