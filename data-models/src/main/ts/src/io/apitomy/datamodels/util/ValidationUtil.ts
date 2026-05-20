/* Generated from Java with JSweet 3.1.0 - http://www.jsweet.org */
import { ModelType } from '../models/ModelType';
import { Node } from '../models/Node';
import { ValidationRule } from '../validation/ValidationRule';
import { ValidationRuleMetaData } from '../validation/ValidationRuleMetaData';
import { ValidationVisitor } from '../validation/ValidationVisitor';

/**
 * A compatibility layer containing methods used when performing validation.  Any code that must
 * be different in Java vs. TS/JS *and* related to validation should go in here.  There is a
 * ValidationCompat.ts file that contains the TS/JS specific implementation of this class.
 * @author eric.wittmann@gmail.com
 * @class
 */
export class ValidationUtil {

    public static instantiate(ruleInfo: ValidationRuleMetaData): ValidationRule {
        return new ruleInfo.ruleClass(ruleInfo);
    }

    public static createValidationVisitorForNode(node: Node): ValidationVisitor {
        return new ValidationVisitor(node.root().modelType());
    }

    public static createValidationVisitor(type: ModelType): ValidationVisitor {
        return new ValidationVisitor(type);
    }

    public static join(delim: string, values: string[]): string {
        return values.join(delim);
    }

    public static joinArray(delim: string, values: string[]): string {
        return ValidationUtil.join(delim, values);
    }

}
ValidationUtil["__class"] = "io.apitomy.datamodels.util.ValidationUtil";
