package LLM.prompts;

public class JsonOutputContract {

    public String getContent() {

        StringBuilder contract = new StringBuilder();

        contract.append("Output contract:\n");
        contract.append("\n");

        contract.append("Return exactly one valid JSON object with the following structure:\n");
        contract.append("\n");

        contract.append("{\n");
        contract.append("  \"cubeName\": \"\",\n");
        contract.append("  \"queryName\": \"\",\n");
        contract.append("  \"aggregateFunction\": \"\",\n");
        contract.append("  \"measure\": \"\",\n");
        contract.append("  \"gamma\": [],\n");
        contract.append("  \"sigma\": []\n");
        contract.append("}\n");

        contract.append("\n");

        contract.append("Mandatory format rules:\n");
        contract.append("- Return only the JSON object.\n");
        contract.append("- Do not include text before or after the JSON object.\n");
        contract.append("- Do not use Markdown code fences.\n");
        contract.append("- Do not add, remove, or rename fields.\n");
        contract.append("- Use double quotes for JSON keys and string values.\n");
        contract.append("- \"gamma\" must always be a JSON array of strings.\n");
        contract.append("- \"sigma\" must always be a JSON array of strings.\n");
        contract.append("- Use [] when gamma or sigma is empty.\n");
        contract.append("- The result must be syntactically valid JSON.\n");
        contract.append("\n");

        contract.append("Before returning the answer, verify that the JSON follows this contract exactly.\n");
        contract.append("Do not output this verification process.\n");

        return contract.toString();
    }
}