// TicketsTabContent.tsx
import {
  Badge,
  Box,
  Button,
  Flex,
  Modal,
  ModalBody,
  ModalCloseButton,
  ModalContent,
  ModalFooter,
  ModalHeader,
  ModalOverlay,
  Spinner,
  TabPanel,
  Text,
  Textarea,
  useToast,
} from "@chakra-ui/react";
import React, { useContext, useEffect, useState } from "react";
import { apiClient, authorise } from "../utils/apiClient";
import { Ticket } from "./Ticket";
import { TicketInterface } from "./types";
import { colors } from "../../theme";
import { UserContext } from "../../App";

interface TicketsTabContentProps {
  tickets: TicketInterface[];
  text: string;
  fakeReload: boolean;
  setFakeReload: (val: boolean) => void;
}

interface TicketDetailsInterface {
  id: number;
  doctorName: string;
  patientName: string;
  title: string;
  description: string;
  response: string;
  status: "OPENED" | "CLOSED";
  specialization: string;
}

export const TicketsTabContent: React.FC<TicketsTabContentProps> = ({
  tickets,
  text,
  fakeReload,
  setFakeReload,
}) => {
  const auth = useContext(UserContext);
  const toast = useToast();

  const [isOpenDetails, setIsOpenDetails] = useState<boolean>(false);
  const [isLoadingDetails, setIsLoadingDetails] = useState<boolean>(true);

  const [selectedTicket, setSelectedTicket] = useState<string | null>(null);
  const [ticketDetails, setTicketDetails] =
    useState<TicketDetailsInterface | null>(null);

  const [isOpenSafety, setIsOpenSafety] = useState<boolean>(false);
  const [response, setResponse] = useState<string>("");

  useEffect(() => {
    if (isOpenDetails && selectedTicket) {
      setIsLoadingDetails(true);
      apiClient
        .get(`/tickets/${selectedTicket}`, authorise())
        .then((res) => {
          setTicketDetails(res.data);
          setIsLoadingDetails(false);
        })
        .catch((err) => {
          setIsOpenDetails(false);
          toast({
            title: err.response.data.error,
            description: err.response.data.message,
            status: "error",
            duration: 3000,
            isClosable: true,
          });
        });
    }
  }, [selectedTicket]);

  const onClose = () => {
    setIsOpenDetails(false);
    setIsLoadingDetails(false);
    setTicketDetails(null);
    setSelectedTicket(null);
    setIsOpenSafety(false);
  };

  const handleOpenTicket = (ticketId: string) => {
    setIsOpenDetails(true);
    setSelectedTicket(ticketId);
  };

  const handleCloseTicket = () => {
    apiClient
      .patch(
        `/tickets/${selectedTicket}`,
        {
          response: response !== "" ? response : null,
          status: "CLOSED",
        },
        authorise()
      )
      .then((res) => {
        setFakeReload(!fakeReload);
        toast({
          title: "Success!",
          description: "Ticket closed successfully.",
          status: "success",
          duration: 3000,
          isClosable: true,
        });
      })
      .catch((err) => {
        console.log("Erro close ticket", err);
        toast({
          title: err.response.data.error,
          description: err.response.data.message,
          status: "error",
          duration: 3000,
          isClosable: true,
        });
      })
      .finally(() => {
        onClose();
      });
  };

  return (
    <TabPanel>
      {/* Ticket details modal */}
      <Modal
        isCentered
        size={"xl"}
        isOpen={isOpenDetails}
        closeOnEsc={false}
        onClose={onClose}
      >
        <ModalOverlay />
        <ModalContent>
          <ModalHeader fontSize={"x-large"}>{ticketDetails?.title}</ModalHeader>
          <ModalCloseButton />
          <ModalBody>
            {isLoadingDetails ? (
              <Spinner />
            ) : (
              <>
                <Badge bg={colors.blue} color="white">
                  {ticketDetails?.specialization}
                </Badge>

                <Flex gap={1}>
                  <Text>
                    {auth.user?.role === "PATIENT"
                      ? "Doctor name: "
                      : "Patient name: "}
                  </Text>
                  <Text fontWeight={700}>
                    {auth.user?.role === "PATIENT"
                      ? ticketDetails?.doctorName
                      : ticketDetails?.patientName}
                  </Text>
                </Flex>

                <Box h={5} />

                <Flex direction={"column"}>
                  <Text fontWeight={700}>Description</Text>
                  <Text>{ticketDetails?.description}</Text>
                </Flex>

                <Box h={10} />

                <Flex direction={"column"}>
                  <Text fontWeight={700}>Response</Text>
                  {auth.user?.role === "DOCTOR" ? (
                    <Textarea
                      value={response}
                      onChange={(e) => {
                        setResponse(e.target.value);
                      }}
                      placeholder="Write your response"
                      minH={"100px"}
                    ></Textarea>
                  ) : (
                    <Text>{ticketDetails?.response ?? "No response yet."}</Text>
                  )}
                </Flex>
              </>
            )}
          </ModalBody>

          <ModalFooter>
            {ticketDetails?.status === "OPENED" && (
              <Button
                variant={"solid"}
                colorScheme="blue"
                onClick={() => {
                  setIsOpenSafety(true);
                  setIsOpenDetails(false);
                }}
                isDisabled={auth.user?.role === "DOCTOR" && response === ""}
              >
                Close ticket
              </Button>
            )}
          </ModalFooter>
        </ModalContent>
      </Modal>

      <Modal
        isCentered
        size={"sm"}
        isOpen={isOpenSafety}
        closeOnEsc={false}
        onClose={onClose}
      >
        <ModalOverlay />
        <ModalContent>
          <ModalHeader fontSize={"x-large"} color={"red"}>
            Warning!
          </ModalHeader>
          <ModalCloseButton />
          <ModalBody>
            <Text fontWeight={900}>
              This action is irreversible! Are you sure you want to close this
              ticket?
            </Text>
          </ModalBody>

          <ModalFooter gap={5}>
            <Button
              variant={"outline"}
              colorScheme="blue"
              onClick={onClose}
              px={5}
            >
              No
            </Button>

            <Button
              variant={"solid"}
              colorScheme="blue"
              onClick={handleCloseTicket}
              px={5}
            >
              Yes
            </Button>
          </ModalFooter>
        </ModalContent>
      </Modal>

      <Text fontSize="4xl" fontWeight="bold" color="black" pl={10}>
        {text}
      </Text>
      <Flex mt={4} pl={10} wrap={"wrap"} gap={5}>
        {tickets.length > 0 ? (
          tickets.map((ticket, i) => (
            <Box key={i} mr={4}>
              <Ticket ticket={ticket} handleOpenTicket={handleOpenTicket} />
            </Box>
          ))
        ) : (
          <Text>{`No ${text.toLowerCase()}`}</Text>
        )}
      </Flex>
    </TabPanel>
  );
};
