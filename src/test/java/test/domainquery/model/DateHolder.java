/************************************************************************
 * Copyright (c) 2015 IoT-Solutions e.U.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ************************************************************************/

package test.domainquery.model;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

public class DateHolder {

	private Date date;
	private Timestamp sqlTimestamp;
	private List<Date> dates;
	private String name;

	public DateHolder() {
		super();
	}

	public DateHolder(String name, Date date) {
		super();
		this.name = name;
		this.date = date;
	}

	public Date getDate() {
		return date;
	}

	public String getName() {
		return name;
	}

	public Timestamp getSqlTimestamp() {
		return sqlTimestamp;
	}

	public void setSqlTimestamp(Timestamp sqlTimestamp) {
		this.sqlTimestamp = sqlTimestamp;
	}

	public List<Date> getDates() {
		return dates;
	}

	public void setDates(List<Date> dates) {
		this.dates = dates;
	}
}
