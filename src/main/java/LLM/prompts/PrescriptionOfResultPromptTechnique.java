package LLM.prompts;

public class PrescriptionOfResultPromptTechnique
        implements PromptTechnique {

    @Override
    public String getName() {
        return PromptTechniqueFactory.PRESCRIPTION_OF_RESULT;
    }

    @Override
    public String getContent() {
        return prescriptionOfResult();
    }

    private String prescriptionOfResult() {

        StringBuilder prescription = new StringBuilder();

        prescription.append("Prescription of result:\n");
        prescription.append("\n");

        prescription.append("Return the result as exactly one valid JSON object.\n");
        prescription.append("The JSON object must contain exactly the following six fields:\n");
        prescription.append("- cubeName\n");
        prescription.append("- queryName\n");
        prescription.append("- aggregateFunction\n");
        prescription.append("- measure\n");
        prescription.append("- gamma\n");
        prescription.append("- sigma\n");
        prescription.append("\n");

        prescription.append("Use exactly the following JSON structure:\n");
        prescription.append("\n");

        prescription.append("{\n");
        prescription.append("  \"cubeName\": \"\",\n");
        prescription.append("  \"queryName\": \"\",\n");
        prescription.append("  \"aggregateFunction\": \"\",\n");
        prescription.append("  \"measure\": \"\",\n");
        prescription.append("  \"gamma\": [],\n");
        prescription.append("  \"sigma\": []\n");
        prescription.append("}\n");

        prescription.append("\n");

        prescription.append("Field requirements:\n");
        prescription.append("\n");

        prescription.append("- \"cubeName\" must contain the cube name selected from the provided cube schema.\n");

        prescription.append("- \"queryName\" must contain a concise single-line name describing the query.\n");
        prescription.append("  The query name does not need to correspond to an element of the cube schema.\n");

        prescription.append("- \"aggregateFunction\" must contain exactly one of the following values:\n");
        prescription.append("  \"Sum\", \"Avg\", \"Count\", \"Min\", or \"Max\".\n");

        prescription.append("- \"measure\" must contain exactly one measure name from the provided cube schema.\n");

        prescription.append("- \"gamma\" must be a JSON array of strings.\n");
        prescription.append("  Each string represents one grouping field.\n");
        prescription.append("  Each grouping field must have the form \"dimensionName.attributeName\".\n");
        prescription.append("  If no grouping field is required, return an empty array: [].\n");

        prescription.append("- \"sigma\" must be a JSON array of strings.\n");
        prescription.append("  Each string represents one filtering condition.\n");
        prescription.append("  Each filtering condition must have the form \"dimensionName.attributeName='value'\".\n");
        prescription.append("  If no filtering condition is required, return an empty array: [].\n");

        prescription.append("\n");

        prescription.append("Output constraints:\n");
        prescription.append("\n");

        prescription.append("- Return only the JSON object and nothing else.\n");
        prescription.append("- Do not include any text before or after the JSON object.\n");
        prescription.append("- Do not use Markdown code fences.\n");
        prescription.append("- Do not include explanations, reasoning, notes, comments, or additional fields.\n");
        prescription.append("- Do not rename, remove, or add JSON fields.\n");
        prescription.append("- Use double quotes for JSON field names and string values.\n");
        prescription.append("- Do not use trailing commas.\n");
        prescription.append("- The output must be syntactically valid JSON.\n");
        prescription.append("\n");

        prescription.append("Before returning the result, verify that:\n");
        prescription.append("- the output is valid JSON;\n");
        prescription.append("- all six required fields are present exactly once;\n");
        prescription.append("- gamma and sigma are JSON arrays;\n");
        prescription.append("- no additional fields or text are present.\n");
        prescription.append("\n");

        prescription.append("If the output does not satisfy these requirements, correct it before returning the final answer.\n");
        prescription.append("Do not output this verification process.\n");

        return prescription.toString();
    }
}