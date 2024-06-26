import { Flex, FormControl, FormLabel, Input } from "@chakra-ui/react";
import { ProfileInterface } from "./types";

interface FormFieldProps {
  label: string;
  type: string;
  profile: ProfileInterface;
  setProfile: (val: ProfileInterface) => void;
  isDisabled?: boolean;
}

export const FormField: React.FC<FormFieldProps> = ({
  label,
  type,
  profile,
  setProfile,
  isDisabled = false,
}) => {
  //get the key for the profile interface from the label
  const profileKey: string = label
    .split(" ")
    .map((word, i) => {
      const lowerCaseWord = word.toLowerCase();
      return i === 0
        ? word.toLowerCase()
        : word.charAt(0).toUpperCase() + lowerCaseWord.slice(1);
    })
    .join("");

  return (
    <>
      <FormControl mt={1}>
        <Flex>
          <FormLabel whiteSpace="nowrap" fontWeight="bold" fontSize="large">
            {label + ": "}
          </FormLabel>
          <Input
            isDisabled={isDisabled}
            type={type}
            name={label}
            value={profile[profileKey].toString()}
            border="none"
            onChange={(e) => {
              setProfile({
                ...profile,
                [profileKey]: e.target.value,
              });
            }}
            outline="none"
            textAlign="right"
          />
        </Flex>
      </FormControl>
      <hr style={{ borderColor: "black", borderWidth: "1px" }} />
    </>
  );
};
