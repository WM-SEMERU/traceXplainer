
export default class TraceModel {

	constructor(model) {
		this.__model = model
		this.__sourceNames = new Set();
		this.__targetNames = new Set();

		Object.keys(this.__model).forEach((sourceName) => {
			this.__sourceNames.add(sourceName)
			Object.keys(this.__model[sourceName]).forEach((targetName) => {
				this.__targetNames.add(targetName);
			})
		})
	}

	getTracesForArtifact(artifactName) {
		if (this.__sourceNames.has(artifactName)) {
			return this.getTracesForSource(artifactName);
		}

		if (this.__targetNames.has(artifactName)) {
			return this.getTracesForTarget(artifactName);
		}

		throw new Error("Invalid artifact name");
	}

	getTracesForSource(sourceName) {
		if (!this.__sourceNames.has(sourceName)) {
			throw new Error("Invalid source name");
		}

		return Object.keys(this.__model[sourceName]).map((targetName) => {
			return {
				artifactType: "src",
				artifactId: targetName,
				traceValue: this.__model[sourceName][targetName],
				
			}
		})
	}

	getTracesForTarget(targetName) {
		const traces = [];
		this.__sourceNames.forEach((sourceName) => {
			const value = this.__model[sourceName][targetName];

			if (value) {
				traces.push({
					artifactType: "req",
					artifactId: sourceName,
					traceValue: value,
				});
			}
		});

		if (traces.length === 0) {
			throw new Error("Invalid target name");
		}

		return traces;
	}

	static async getInstanceFromFile(tmFileURL) {
		const tmFile = await fetch(tmFileURL, {mode: 'no-cors'});
		const fileContent = await tmFile.text();

		const model = {};
		const lines = fileContent.split(/\r?\n/);

		lines.forEach((line) => {
			const lineTokens = line.split(' ');
			if (lineTokens[0] === '#') {
				// TODO: Parse tm metadata
				return;
			}

			const sourceName = lineTokens[0];
			const targetName = lineTokens[1];
			const traceValue = parseFloat(lineTokens[2]);

			if (!(sourceName in model)) {
				model[sourceName] = {};
			}

			model[sourceName][targetName] = traceValue;
		});

		return new TraceModel(model);
	}
}