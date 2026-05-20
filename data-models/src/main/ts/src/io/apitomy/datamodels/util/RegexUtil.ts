
export class RegexUtil {
    
    public static matches(value: string, regex: string): boolean {
        let matchRegex: RegExp = new RegExp(regex);
        return matchRegex.test(value);
    }
    
    /**
     * Finds all strings within the given input that match the given regular expression.  Returns
     * all matches, with each match represented as a list of the groups specified in the regex.
     * @param value
     * @param regex
     */
    public static findMatches(value: string, regex: string): string[][] {
        let rval: string[][] = [];
        let matchRegex: RegExp = new RegExp(regex, 'g');
        let match: string[] = matchRegex.exec(value);
        while (match) {
            rval.push(match);
            match = matchRegex.exec(value);
        }
        return rval;
    }

}
