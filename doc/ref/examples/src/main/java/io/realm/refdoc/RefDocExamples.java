package io.realm.refdoc;

public class RefDocExamples {

    public static void main(String[] args) {
        System.out.println("------ Running all reference documentation examples. ------");

        try {
            TypedTableIntro.main(null);
            TypedTableExamples.main(null);

            TypedTableViewIntro.main(null);

            TypedQueryIntro.main(null);
            TypedQueryExamples.main(null);

            DynTableIntro.main(null);

            DynTableViewIntro.main(null);
            DynTableViewExamples.main(null);

            DynQueryIntro.main(null);
            DynQueryExamples.main(null);

            GroupIntro.main(null);
            GroupExamples.main(null);

            SharedGroupIntro.main(null);
            SharedGroupExamples.main(null);

            WriteTransactionIntro.main(null);
            WriteTransactionExamples.main(null);

            ReadTransactionExamples.main(null);

            DynamicReadTransactionIntro.main(null);
            TypedReadTransactionIntro.main(null);

            System.out.println("------ Successfully executed all ref-doc examples ------");

        } catch (Exception e) {
            System.err.println("!!!!! Ref-doc example FAILED !!!!!");
            e.printStackTrace(System.err);
            throw new RuntimeException();
        }
    }
}
