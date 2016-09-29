package com.demonwav.mcdev.platform.mixin.util;

import com.demonwav.mcdev.platform.mixin.inspections.ShadowInspection;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;

import java.util.List;

public final class ShadowError {

    private Object[] errorContextInfos;

    public Object[] getErrorContextInfos() {
        return errorContextInfos;
    }

    public void setErrorContextInfos(Object[] errorContextInfos) {
        this.errorContextInfos = errorContextInfos;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private List<Object> errorContextInfos = Lists.newArrayList();

        public ShadowError build() {
            ShadowError error = new ShadowError();
            error.setErrorContextInfos(errorContextInfos.toArray());
            return error;
        }

        public Builder addContext(Object context) {
            errorContextInfos.add(context);
            return this;
        }
    }

    public enum Key {
        MULTI_TARGET_CLASS_REMAPPED_TRUE,
        MULTI_TARGET_SHADOW_REMAPPED_TRUE,
        NO_SHADOW_METHOD_FOUND_WITH_REMAP,
        NO_MATCHING_METHODS_FOUND,
        INVALID_ACCESSOR_ON_SHADOW_METHOD,
        CANNOT_FIND_MIXIN_TARGET,
        NOT_MIXIN_CLASS,
        NO_MIXIN_CLASS_TARGETS,
        NO_SHADOW_FIELD_FOUND_WITH_REMAP,
        INVALID_ACCESSOR_ON_SHADOW_FIELD,
        INVALID_FIELD_TYPE,
        NO_FINAL_ANNOTATION_WITH_FINAL_TARGET
    }

    public static final class ErrorMessages {
        public static String formatError(Object... args) {
            if (args.length == 0) {
                return "Error";
            }

            final Object first = args[0];
            if (!(first instanceof Key)) {
                return "Error";
            }
            return FORMATTERS.stream()
                .filter(formatter -> formatter.matches((Key) first))
                .findFirst()
                .map(found -> found.formatMessage(args))
                .orElse("Error");
        }

        public static final List<Formatter> FORMATTERS = ImmutableList.<Formatter>builder()
            .add(new Formatter(Key.MULTI_TARGET_CLASS_REMAPPED_TRUE) {
                @Override
                String formatMessage(Object... args) {
                    Preconditions.checkArgument(args[0].equals(Key.MULTI_TARGET_CLASS_REMAPPED_TRUE));
                    final PsiClass containingClass = (PsiClass) args[1];
                    final String containingName = containingClass.getName();

                    return "Cannot have a shadow when " + containingName + " is mixing into multiple remapped targets.";
                }
            })
            .add(new Formatter(Key.MULTI_TARGET_SHADOW_REMAPPED_TRUE) {
                @Override
                String formatMessage(Object... args) {
                    Preconditions.checkArgument(args[0].equals(Key.MULTI_TARGET_SHADOW_REMAPPED_TRUE));
                    final PsiClass containingClass = (PsiClass) args[1];
                    final String containingName = containingClass.getName();

                    return "Cannot have a remapped shadow when " + containingName + " is mixing into multiple targets.";
                }
            })
            .add(new Formatter(Key.NO_SHADOW_METHOD_FOUND_WITH_REMAP) {
                @Override
                String formatMessage(Object... args) {
                    Preconditions.checkArgument(args[0].equals(Key.NO_SHADOW_METHOD_FOUND_WITH_REMAP));
                    final String methodName = (String) args[1];
                    final String targetClassName = (String) args[2];

                    return "No method found by the name: " + methodName + " in target classes: " + targetClassName;
                }
            })
            .add(new Formatter(Key.NO_MATCHING_METHODS_FOUND) {
                @Override
                String formatMessage(Object... args) {
                    Preconditions.checkArgument(args[0].equals(Key.NO_MATCHING_METHODS_FOUND));
                    final String methodName = (String) args[1];
                    final String targetClassName = (String) args[2];
                    final PsiMethod[] foundMethods = (PsiMethod[]) args[3];
                    return "No methods found matching signature: " + methodName + " in target classes: " + targetClassName + ".";
                }
            })
            .add(new Formatter(Key.INVALID_ACCESSOR_ON_SHADOW_METHOD) {
                @Override
                String formatMessage(Object... args) {
                    Preconditions.checkArgument(args[0].equals(Key.INVALID_ACCESSOR_ON_SHADOW_METHOD));
                    final String current = (String) args[1];
                    final String expected = (String) args[2];
                    return "Method has invalid access modifiers, has: " + current + " but target method has: " + expected + ".";
                }
            })
            .add(new Formatter(Key.CANNOT_FIND_MIXIN_TARGET) {
                @Override
                String formatMessage(Object... args) {
                    Preconditions.checkArgument(args[0].equals(Key.CANNOT_FIND_MIXIN_TARGET));
                    return "Cannot shadow nonexistent target: target class undefined";
                }
            })
            .add(new Formatter(Key.NOT_MIXIN_CLASS) {
                @Override
                String formatMessage(Object... args) {
                    Preconditions.checkArgument(args[0].equals(Key.NOT_MIXIN_CLASS));
                    return "Cannot shadow anything in a non-@Mixin annotated class.";
                }
            })
            .add(new Formatter(Key.NO_MIXIN_CLASS_TARGETS) {
                @Override
                String formatMessage(Object... args) {
                    Preconditions.checkArgument(args[0].equals(Key.NO_MIXIN_CLASS_TARGETS));
                    return "Cannot shadow anything when the Mixin class has no targets.";
                }
            })
            .add(new Formatter(Key.NO_SHADOW_FIELD_FOUND_WITH_REMAP) {
                @Override
                String formatMessage(Object... args) {
                    Preconditions.checkArgument(args[0].equals(Key.NO_SHADOW_FIELD_FOUND_WITH_REMAP));
                    final String methodName = (String) args[1];
                    final String targetClassName = (String) args[2];

                    return "No field found by the name: " + methodName + " in target classes: " + targetClassName;
                }
            })
            .add(new Formatter(Key.INVALID_ACCESSOR_ON_SHADOW_FIELD) {
                @Override
                String formatMessage(Object... args) {
                    Preconditions.checkArgument(args[0].equals(Key.INVALID_ACCESSOR_ON_SHADOW_FIELD));
                    final String current = (String) args[1];
                    final String expected = (String) args[2];
                    return "Field has invalid access modifiers, has: " + current + " but target field has: " + expected + ".";
                }
            })
            .add(new Formatter(Key.INVALID_FIELD_TYPE) {
                @Override
                String formatMessage(Object... args) {
                    Preconditions.checkArgument(args[0].equals(Key.INVALID_FIELD_TYPE));
                    final String fieldType = (String) args[1];
                    final String targetType = (String) args[2];
                    return "Field has invalid type, has " + fieldType + " but target field has " + targetType + ".";
                }
            })
            .add(new Formatter(Key.NO_FINAL_ANNOTATION_WITH_FINAL_TARGET) {
                @Override
                String formatMessage(Object... args) {
                    Preconditions.checkArgument(args[0].equals(Key.NO_FINAL_ANNOTATION_WITH_FINAL_TARGET));
                    return "Field targeting a final target field is not annotated with @Final";
                }
            })
            .build();
    }

    public static abstract class Formatter {
        final Key key;

        Formatter(Key key) {
            this.key = key;
        }

        boolean matches(Key key) {
            return this.key == key;
        }

        abstract String formatMessage(Object... args);
    }
}