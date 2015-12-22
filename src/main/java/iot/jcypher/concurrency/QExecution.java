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

package iot.jcypher.concurrency;

public class QExecution {

	public static final String REPLAY_QUERY = "_REPLAY_QUERY";
	
	private ExecType execType;
	private boolean checkedReloadModel;
	
	public QExecution(ExecType execType) {
		super();
		this.execType = execType;
		this.checkedReloadModel = false;
	}
	
	public ExecType geExecType() {
		return this.execType;
	}

	public boolean isCheckedReloadModel() {
		return checkedReloadModel;
	}

	public void setCheckedReloadModel(boolean checkedReloadModel) {
		this.checkedReloadModel = checkedReloadModel;
	}

	/***********************************/
	public enum ExecType {
		execute(true), executeCount(true), resultOf(false);
		
		private boolean checkForReload;
		
		private ExecType(boolean cfr) {
			this.checkForReload = cfr;
		}

		public boolean shouldCheckForReload() {
			return checkForReload;
		}
	}
}
