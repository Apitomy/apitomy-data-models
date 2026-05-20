import {ValidationProblemSeverity} from "../../src/io/apitomy/datamodels/validation/ValidationProblemSeverity";
import {ValidationProblem} from "../../src/io/apitomy/datamodels/validation/ValidationProblem";

const fs = require("fs");

export function readJSON(path: string): any {
    let fileContent: string = fs.readFileSync(path);
    return JSON.parse(fileContent);
}

export function readTXT(path: string): string {
    let fileContent: string = fs.readFileSync(path, "utf8");
    if (typeof fileContent == "string") {
        return fileContent;
    }
    console.warn("WARNING: Failed to read text file: " + path);
    return "";
}


export function formatSeverity(severity: ValidationProblemSeverity): string {
    if (severity == ValidationProblemSeverity.ignore) {
        return "ignore";
    } else if (severity == ValidationProblemSeverity.low) {
        return "low";
    } else if (severity == ValidationProblemSeverity.medium) {
        return "medium";
    } else if (severity == ValidationProblemSeverity.high) {
        return "high";
    }
}


export function readSeverity(severity: string): ValidationProblemSeverity {
    if (severity == "ignore") {
        return ValidationProblemSeverity.ignore;
    } else if (severity == "low") {
        return ValidationProblemSeverity.low;
    } else if (severity == "medium") {
        return ValidationProblemSeverity.medium;
    } else if (severity == "high") {
        return ValidationProblemSeverity.high;
    }
}


export function formatProblems(problems: ValidationProblem[]): string[] {
    let es: string[] = [];
    problems.forEach(problem => {
        es.push(`[${problem.errorCode}] |${formatSeverity(problem.severity)}| {${problem.nodePath.toString(true)}->${problem.property}} :: ${problem.message}`);
    });
    return es;
}

