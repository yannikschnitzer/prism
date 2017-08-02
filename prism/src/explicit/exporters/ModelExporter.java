package explicit.exporters;

import java.io.File;
import java.util.BitSet;

import prism.PrismException;
import prism.PrismFileLog;
import prism.PrismLog;
import explicit.Model;

public abstract class ModelExporter
{
	/** The model to be exported */
	protected Model model;

	/** The original model (as opposed to a processed/normalized model).
	  * The export will be restricted to this model iff it is non-null */
	protected Model originalModel = null;

	/** Formatter to be used for exporting the states in the model */
	protected StateExporter exp = new StateExporter();

	public ModelExporter(Model model)
	{
		this.model = model;
	}

	/**
	 * Replaces the default state exporter (which only prints the states id) with a new state
	 * exporter, e.g. for printing variable valuations instead of state indices.
	 * @param exp New state exporter
	 */
	public void setStateExporter(StateExporter exp)
	{
		this.exp = exp;
	}

	public void restrictExportTo(Model originalModel)
	{
		this.originalModel = originalModel;
	}

	/**
	 * Export to a dot file, highlighting states in 'mark'.
	 * @param out PrismLog to export to
	 * @param mark States to highlight (ignored if null)
	 * @param showStates Show state info on nodes?
	 */
	public void exportToDotFile(PrismLog out, BitSet mark, boolean showStates)
	{
		throw new UnsupportedOperationException();
	}

	public static ModelExporter makeExporter(Model model, ExportType modelExportType) throws PrismException
	{
		switch (modelExportType) {
		case DOT:
			return new DotModelExporter(model);
		case DOT_STATES:
			break;
		case EXPLICIT_TRA:
			return new ExplicitTraModelExporter(model);
		case MATLAB:
			break;
		case MRMC:
			break;
		case PRISM_LANG:
			return new PrismLanguageModelExporter(model);
		case ROWS:
			break;
		case SMT:
			return new Smt2ModelExporter(model);
		default:
			break;
		}
		throw new PrismException("There is no exporter for " + modelExportType); // TODO: tidy 
	}

	/**
	 * Export the model to a PrismLog.
	 */
	public abstract void export(PrismLog out) throws PrismException;

	/**
	 * Export the model to a file.
	 */
	public void export(File file) throws PrismException
	{
		PrismFileLog out = PrismFileLog.create(file.getPath());
		export(out);
		out.close();
	}

	/**
	 * Export the model to a file.
	 */
	public void export(String filename) throws PrismException
	{
		export(new File(filename));
	}

	public void exportWithStrat(PrismLog out, int[] strat) throws PrismException
	{
		// Not implemented by default 
		throw new UnsupportedOperationException("Model export with strategies is not yet supported for " + model.getModelType() + "s in the explicit engine.");
	}
}
